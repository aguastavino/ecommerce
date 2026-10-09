package it.ecommerce.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.ecommerce.adapter.OrderAdapter;
import it.ecommerce.dto.OrderRequest;
import it.ecommerce.dto.OrderResponse;
import it.ecommerce.facade.ECommerceFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order management")
public class OrderController {

    private final ECommerceFacade facade;
    private final OrderAdapter adapter;

    @Operation(summary = "Create an order",
            description = "Fails with 409 when the stock of any requested product is insufficient. "
                    + "On success the stock of each product is decreased by the ordered quantity.")
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

    @Operation(summary = "List all orders")
    @ApiResponse(responseCode = "200", description = "List of orders")
    @GetMapping
    public List<OrderResponse> findAll() {
        return facade.getOrders().stream().map(adapter::toResponse).toList();
    }
}
