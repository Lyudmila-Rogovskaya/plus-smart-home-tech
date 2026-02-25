package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.ProductCategory;
import ru.yandex.practicum.dto.ProductDto;
import ru.yandex.practicum.dto.QuantityState;
import ru.yandex.practicum.dto.SetProductQuantityStateRequest;
import ru.yandex.practicum.service.ProductService;
import jakarta.validation.Valid;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/shopping-store")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public Page<ProductDto> getProducts(@RequestParam ProductCategory category,
                                        Pageable pageable) {
        return productService.getProducts(category, pageable);
    }

    @GetMapping("/{productId}")
    public ProductDto getProduct(@PathVariable String productId) {
        return productService.getProduct(productId);
    }

    @PutMapping
    public ProductDto createNewProduct(@Valid @RequestBody ProductDto productDto) {
        return productService.createProduct(productDto);
    }

    @PostMapping
    public ProductDto updateProduct(@Valid @RequestBody ProductDto productDto) {
        return productService.updateProduct(productDto);
    }

    @PostMapping(value = "/removeProductFromStore", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Boolean removeProductFromStore(@RequestBody UUID productId) {
        return productService.removeProduct(productId);
    }

    @PostMapping(value = "/quantityState", consumes = {"application/json", MediaType.ALL_VALUE})
    public Boolean setProductQuantityState(
            @RequestBody(required = false) SetProductQuantityStateRequest request,
            @RequestParam(required = false) UUID productId,
            @RequestParam(required = false) QuantityState quantityState) {

        if (request != null) {
            return productService.setQuantityState(request);
        } else if (productId != null && quantityState != null) {
            SetProductQuantityStateRequest req = new SetProductQuantityStateRequest();
            req.setProductId(productId);
            req.setQuantityState(quantityState);
            return productService.setQuantityState(req);
        } else {
            throw new IllegalArgumentException("Either request body or query parameters (productId, quantityState) must be provided");
        }
    }

}
