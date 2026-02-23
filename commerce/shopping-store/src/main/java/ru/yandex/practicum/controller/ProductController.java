package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.ProductCategory;
import ru.yandex.practicum.dto.ProductDto;
import ru.yandex.practicum.dto.SetProductQuantityStateRequest;
import ru.yandex.practicum.service.ProductService;

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
    public ProductDto createNewProduct(@RequestBody ProductDto productDto) {
        return productService.createProduct(productDto);
    }

    @PostMapping
    public ProductDto updateProduct(@RequestBody ProductDto productDto) {
        return productService.updateProduct(productDto);
    }

    @PostMapping("/removeProductFromStore")
    public Boolean removeProductFromStore(@RequestBody String productId) {
        return productService.removeProduct(productId);
    }

    @PostMapping("/quantityState")
    public Boolean setProductQuantityState(@RequestBody SetProductQuantityStateRequest request) {
        return productService.setQuantityState(request);
    }

}
