package it.fr.awesomepizza.service;

import it.fr.awesomepizza.dto.request.CreateOrderRequest;
import it.fr.awesomepizza.dto.request.OrderItemRequest;
import it.fr.awesomepizza.dto.response.OrderResponse;
import it.fr.awesomepizza.exception.InvalidOrderStateTransitionException;
import it.fr.awesomepizza.exception.OrderAlreadyInProgressException;
import it.fr.awesomepizza.exception.OrderNotFoundException;
import it.fr.awesomepizza.exception.PizzaNotAvailableException;
import it.fr.awesomepizza.exception.PizzaNotFoundException;
import it.fr.awesomepizza.model.Order;
import it.fr.awesomepizza.model.OrderItem;
import it.fr.awesomepizza.model.OrderStatus;
import it.fr.awesomepizza.model.Pizza;
import it.fr.awesomepizza.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CacheService cacheService;

    @InjectMocks
    private OrderService orderService;

    private Pizza margherita;

    @BeforeEach
    void setUp() {
        margherita = Pizza.of("Margherita", "Pomodoro, mozzarella", new BigDecimal("6.00"));
        margherita.setId(1L);
    }

    @Test
    void createOrder_generatesPublicCodeAndReceivedStatus() {
        when(cacheService.findPizzaById(1L)).thenReturn(Optional.of(margherita));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateOrderRequest request = new CreateOrderRequest(List.of(new OrderItemRequest(1L, 2)));

        OrderResponse response = orderService.createOrder(request);

        assertThat(response.orderCode()).matches("[0-9a-f-]{36}");
        assertThat(response.status()).isEqualTo(OrderStatus.RECEIVED);
        assertThat(response.items()).hasSize(1);
        assertThat(response.totalPrice()).isEqualByComparingTo("12.00");
    }

    @Test
    void createOrder_failsWhenPizzaDoesNotExist() {
        when(cacheService.findPizzaById(99L)).thenReturn(Optional.empty());

        CreateOrderRequest request = new CreateOrderRequest(List.of(new OrderItemRequest(99L, 1)));

        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(PizzaNotFoundException.class);
    }

    @Test
    void createOrder_failsWhenPizzaNotAvailable() {
        margherita.setAvailable(false);
        when(cacheService.findPizzaById(1L)).thenReturn(Optional.of(margherita));

        CreateOrderRequest request = new CreateOrderRequest(List.of(new OrderItemRequest(1L, 1)));

        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(PizzaNotAvailableException.class);
    }

    @Test
    void createOrder_persistedTotalDoesNotChangeWhenListPriceChangesLater() {
        when(cacheService.findPizzaById(1L)).thenReturn(Optional.of(margherita));

        ArgumentCaptor<Order> savedOrderCaptor = ArgumentCaptor.forClass(Order.class);
        when(orderRepository.save(savedOrderCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        CreateOrderRequest request = new CreateOrderRequest(List.of(new OrderItemRequest(1L, 2)));
        OrderResponse createdResponse = orderService.createOrder(request);
        assertThat(createdResponse.totalPrice()).isEqualByComparingTo("12.00");

        // il prezzo di listino della pizza cambia DOPO l'ordine...
        margherita.setPrice(new BigDecimal("9.00"));

        // ...ma il totale già cristallizzato sull'ordine persistito (letto dall'entità, non ricalcolato dagli item) non deve variare
        Order persistedOrder = savedOrderCaptor.getValue();
        String generatedCode = persistedOrder.getOrderCode();
        when(orderRepository.findByOrderCode(generatedCode)).thenReturn(Optional.of(persistedOrder));
        OrderResponse reReadResponse = orderService.getOrderByCode(generatedCode);

        assertThat(reReadResponse.totalPrice()).isEqualByComparingTo("12.00");
    }

    @Test
    void createOrder_mergesSamePizzaRepeatedInMultipleLines() {
        when(cacheService.findPizzaById(1L)).thenReturn(Optional.of(margherita));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // due righe separate per la stessa pizza: vengono accorpate in un unico OrderItem con quantità sommata
        CreateOrderRequest request = new CreateOrderRequest(List.of(
                new OrderItemRequest(1L, 1),
                new OrderItemRequest(1L, 2)
        ));

        OrderResponse response = orderService.createOrder(request);

        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).quantity()).isEqualTo(3);
        assertThat(response.totalPrice()).isEqualByComparingTo("18.00");
    }

    @Test
    void takeInCharge_succeedsWhenNoOrderInProgress() {
        Order order = buildReceivedOrder("ABCD1234");
        when(orderRepository.findByOrderCodeForUpdate("ABCD1234")).thenReturn(Optional.of(order));
        when(orderRepository.existsByStatus(OrderStatus.IN_PROGRESS)).thenReturn(false);

        OrderResponse response = orderService.takeInCharge("ABCD1234");

        assertThat(response.status()).isEqualTo(OrderStatus.IN_PROGRESS);
    }

    @Test
    void takeInCharge_failsWhenAnotherOrderAlreadyInProgress() {
        Order order = buildReceivedOrder("ABCD1234");
        when(orderRepository.findByOrderCodeForUpdate("ABCD1234")).thenReturn(Optional.of(order));
        when(orderRepository.existsByStatus(OrderStatus.IN_PROGRESS)).thenReturn(true);

        assertThatThrownBy(() -> orderService.takeInCharge("ABCD1234"))
                .isInstanceOf(OrderAlreadyInProgressException.class);
    }

    @Test
    void takeInCharge_failsWhenOrderNotInReceivedStatus() {
        Order order = buildReceivedOrder("ABCD1234");
        order.setStatus(OrderStatus.READY);
        when(orderRepository.findByOrderCodeForUpdate("ABCD1234")).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.takeInCharge("ABCD1234"))
                .isInstanceOf(InvalidOrderStateTransitionException.class);
    }

    @Test
    void takeInCharge_failsWhenCodeDoesNotExist() {
        when(orderRepository.findByOrderCodeForUpdate("XXXX")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.takeInCharge("XXXX"))
                .isInstanceOf(OrderNotFoundException.class);
    }

    @Test
    void markReady_succeedsFromInProgress() {
        Order order = buildReceivedOrder("ABCD1234");
        order.setStatus(OrderStatus.IN_PROGRESS);
        when(orderRepository.findByOrderCodeForUpdate("ABCD1234")).thenReturn(Optional.of(order));

        OrderResponse response = orderService.markReady("ABCD1234");

        assertThat(response.status()).isEqualTo(OrderStatus.READY);
    }

    @Test
    void markReady_failsWhenOrderNotInProgress() {
        Order order = buildReceivedOrder("ABCD1234");
        when(orderRepository.findByOrderCodeForUpdate("ABCD1234")).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.markReady("ABCD1234"))
                .isInstanceOf(InvalidOrderStateTransitionException.class);
    }

    @Test
    void markReady_failsWhenOrderAlreadyReady() {
        // stato terminale: un ordine già evaso non può essere segnato pronto una seconda volta
        Order order = buildReceivedOrder("ABCD1234");
        order.setStatus(OrderStatus.READY);
        when(orderRepository.findByOrderCodeForUpdate("ABCD1234")).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.markReady("ABCD1234"))
                .isInstanceOf(InvalidOrderStateTransitionException.class);
    }

    @Test
    void getQueue_ordersByCreatedAtAscending() {
        Order older = buildReceivedOrder("OLD11111");
        Order newer = buildReceivedOrder("NEW22222");
        newer.setCreatedAt(older.getCreatedAt().plus(1, ChronoUnit.MINUTES));
        when(orderRepository.findByStatusOrderByCreatedAtAsc(OrderStatus.RECEIVED))
                .thenReturn(List.of(older, newer));

        List<OrderResponse> queue = orderService.getQueue();

        assertThat(queue).extracting(OrderResponse::orderCode).containsExactly("OLD11111", "NEW22222");
    }

    private Order buildReceivedOrder(String orderCode) {
        Order order = new Order();
        order.setOrderCode(orderCode);
        order.setStatus(OrderStatus.RECEIVED);
        Instant now = Instant.now();
        order.setCreatedAt(now);
        order.setStatusUpdatedAt(now);

        OrderItem item = new OrderItem();
        item.setPizza(margherita);
        item.setQuantity(1);
        item.setUnitPriceSnapshot(margherita.getPrice());
        order.addItem(item);

        return order;
    }
}
