package it.ecommerce.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.ecommerce.adapter.ProductAdapter;
import it.ecommerce.dto.ProductRequest;
import it.ecommerce.dto.ProductResponse;
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
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Product management")
public class ProductController {

    private final ECommerceFacade facade;
    private final ProductAdapter adapter;

    @Operation(summary = "Add a product")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Product created"),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
            @ApiResponse(responseCode = "409", description = "Product code already in use", content = @Content)})
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse add(@Valid @RequestBody ProductRequest request) {
        return adapter.toResponse(facade.addProduct(adapter.toEntity(request)));
    }

    @Operation(summary = "List all products")
    @ApiResponse(responseCode = "200", description = "List of products")
    @GetMapping
    public List<ProductResponse> findAll() {
        return facade.getProducts().stream().map(adapter::toResponse).toList();
    }
}
