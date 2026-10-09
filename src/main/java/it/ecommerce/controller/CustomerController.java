package it.ecommerce.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.ecommerce.adapter.CustomerAdapter;
import it.ecommerce.dto.CustomerRequest;
import it.ecommerce.dto.CustomerResponse;
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
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Tag(name = "Customers", description = "Customer management")
public class CustomerController {

    private final ECommerceFacade facade;
    private final CustomerAdapter adapter;

    @Operation(summary = "Add a customer")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Customer created"),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @io.swagger.v3.oas.annotations.media.Content),
            @ApiResponse(responseCode = "409", description = "Email or tax code already in use", content = @io.swagger.v3.oas.annotations.media.Content)})
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerResponse add(@Valid @RequestBody CustomerRequest request) {
        return adapter.toResponse(facade.addCustomer(adapter.toEntity(request)));
    }

    @Operation(summary = "List all customers")
    @ApiResponse(responseCode = "200", description = "List of customers")
    @GetMapping
    public List<CustomerResponse> findAll() {
        return facade.getCustomers().stream().map(adapter::toResponse).toList();
    }
}
