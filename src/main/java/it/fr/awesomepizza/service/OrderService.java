package it.fr.awesomepizza.service;

import it.fr.awesomepizza.dto.request.CreateOrderRequest;
import it.fr.awesomepizza.dto.request.OrderItemRequest;
import it.fr.awesomepizza.dto.response.OrderItemResponse;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CacheService cacheService;

    public OrderService(OrderRepository orderRepository, CacheService cacheService) {
        this.orderRepository = orderRepository;
        this.cacheService = cacheService;
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        Order order = new Order();
        order.setOrderCode(UUID.randomUUID().toString());
        order.setStatus(OrderStatus.RECEIVED);

        Map<Long, OrderItem> itemsByPizzaId = new HashMap<>();

        for (OrderItemRequest itemRequest : request.items()) {
            Pizza pizza = cacheService.findPizzaById(itemRequest.pizzaId())
                    .orElseThrow(() -> new PizzaNotFoundException(itemRequest.pizzaId()));
            if (!pizza.isAvailable()) {
                throw new PizzaNotAvailableException(pizza.getName());
            }

            OrderItem existingItem = itemsByPizzaId.get(itemRequest.pizzaId());
            if (existingItem != null) {
                existingItem.setQuantity(existingItem.getQuantity() + itemRequest.quantity());
                continue;
            }

            OrderItem item = new OrderItem();
            item.setPizza(pizza);
            item.setQuantity(itemRequest.quantity());
            item.setUnitPriceSnapshot(pizza.getPrice());

            order.addItem(item);
            itemsByPizzaId.put(itemRequest.pizzaId(), item);
        }

        Order saved = orderRepository.save(order);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderByCode(String orderCode) {
        return toResponse(findOrderOrThrow(orderCode));
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getQueue() {
        return orderRepository.findByStatusOrderByCreatedAtAsc(OrderStatus.RECEIVED).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public synchronized OrderResponse takeInCharge(String orderCode) {
        Order order = findOrderForUpdateOrThrow(orderCode);

        if (!order.getStatus().canTransitionTo(OrderStatus.IN_PROGRESS)) {
            throw new InvalidOrderStateTransitionException(order.getStatus(), OrderStatus.IN_PROGRESS);
        }
        if (orderRepository.existsByStatus(OrderStatus.IN_PROGRESS)) {
            throw new OrderAlreadyInProgressException();
        }

        order.setStatus(OrderStatus.IN_PROGRESS);
        return toResponse(order);
    }

    @Transactional
    public OrderResponse markReady(String orderCode) {
        Order order = findOrderForUpdateOrThrow(orderCode);

        if (!order.getStatus().canTransitionTo(OrderStatus.READY)) {
            throw new InvalidOrderStateTransitionException(order.getStatus(), OrderStatus.READY);
        }

        order.setStatus(OrderStatus.READY);
        return toResponse(order);
    }

    private Order findOrderOrThrow(String orderCode) {
        return orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new OrderNotFoundException(orderCode));
    }

    private Order findOrderForUpdateOrThrow(String orderCode) {
        return orderRepository.findByOrderCodeForUpdate(orderCode)
                .orElseThrow(() -> new OrderNotFoundException(orderCode));
    }

    private OrderResponse toResponse(Order order) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(item -> new OrderItemResponse(
                        item.getPizza().getName(),
                        item.getQuantity(),
                        item.getUnitPriceSnapshot(),
                        item.getUnitPriceSnapshot().multiply(BigDecimal.valueOf(item.getQuantity()))
                ))
                .toList();

        BigDecimal totalPrice = items.stream()
                .map(OrderItemResponse::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new OrderResponse(
                order.getOrderCode(),
                order.getStatus(),
                order.getCreatedAt(),
                order.getStatusUpdatedAt(),
                items,
                totalPrice
        );
    }
}
