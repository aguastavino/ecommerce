package it.ecommerce.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.ecommerce.adapter.OrderAdapter;
import it.ecommerce.dto.OrderEventRequest;
import it.ecommerce.dto.OrderRequest;
import it.ecommerce.dto.OrderResponse;
import it.ecommerce.dto.PageResponse;
import it.ecommerce.facade.ECommerceFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order management")
public class OrderController {

    private final ECommerceFacade facade;
    private final OrderAdapter adapter;

    @Operation(summary = "Create an order",
            description = "Fails with 409 when the stock of any requested product is insufficient. "
                    + "On success the stock of each product is decreased by the ordered quantity. "
                    + "The new order is in status ORDINATO.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Order created"),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
            @ApiResponse(responseCode = "404", description = "Customer or product not found", content = @Content),
            @ApiResponse(responseCode = "409", description = "Insufficient stock", content = @Content)})
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse create(@Valid @RequestBody OrderRequest request) {
        return adapter.toResponse(facade.createOrder(adapter.toCommand(request)));
    }

    @Operation(summary = "List all orders", description = "Supports pagination through page, size and sort.")
    @ApiResponse(responseCode = "200", description = "Page of orders")
    @GetMapping
    public PageResponse<OrderResponse> findAll(
            @ParameterObject @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return PageResponse.of(facade.getOrders(pageable), adapter::toResponse);
    }

    @Operation(summary = "Register an event on an order",
            description = "DELIVERED moves the order from ORDINATO to CONSEGNATO.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order updated"),
            @ApiResponse(responseCode = "404", description = "Order not found", content = @Content),
            @ApiResponse(responseCode = "409", description = "Event not allowed in the current status", content = @Content)})
    @PostMapping("/{id}/events")
    public OrderResponse handleEvent(@PathVariable Long id, @Valid @RequestBody OrderEventRequest request) {
        return adapter.toResponse(facade.handleOrderEvent(id, request.type()));
    }

    @Operation(summary = "Cancel an order",
            description = "Not allowed once the order is CONSEGNATO. The reserved stock is given back.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Order cancelled"),
            @ApiResponse(responseCode = "404", description = "Order not found", content = @Content),
            @ApiResponse(responseCode = "409", description = "Order already delivered", content = @Content)})
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(@PathVariable Long id) {
        facade.cancelOrder(id);
    }
}
