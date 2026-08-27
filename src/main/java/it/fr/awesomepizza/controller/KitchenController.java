package it.fr.awesomepizza.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.fr.awesomepizza.dto.response.OrderResponse;
import it.fr.awesomepizza.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/kitchen/orders")
@Tag(name = "Pizzaiolo", description = "Gestione della coda ordini e delle transizioni di stato da parte del pizzaiolo")
public class KitchenController {

    private final OrderService orderService;

    public KitchenController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/queue")
    @Operation(summary = "Elenca gli ordini in coda (RECEIVED), in ordine FIFO")
    public ResponseEntity<List<OrderResponse>> getQueue() {
        return ResponseEntity.ok(orderService.getQueue());
    }

    @PutMapping("/{orderCode}/take-in-charge")
    @Operation(summary = "Prende in carico un ordine (fallisce se un altro ordine è già in lavorazione)")
    public ResponseEntity<OrderResponse> takeInCharge(@PathVariable String orderCode) {
        return ResponseEntity.ok(orderService.takeInCharge(orderCode));
    }

    @PutMapping("/{orderCode}/ready")
    @Operation(summary = "Imposta un ordine come pronto")
    public ResponseEntity<OrderResponse> markReady(@PathVariable String orderCode) {
        return ResponseEntity.ok(orderService.markReady(orderCode));
    }
}
