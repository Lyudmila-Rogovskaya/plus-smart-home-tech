package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.ProductCategory;
import ru.yandex.practicum.dto.ProductDto;
import ru.yandex.practicum.dto.ProductState;
import ru.yandex.practicum.dto.SetProductQuantityStateRequest;
import ru.yandex.practicum.entity.Product;
import ru.yandex.practicum.exception.ProductNotFoundException;
import ru.yandex.practicum.mapper.ProductMapper;
import ru.yandex.practicum.repository.ProductRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public Page<ProductDto> getProducts(ProductCategory category, Pageable pageable) {
        return productRepository.findByProductCategory(category, pageable)
                .map(productMapper::toDto);
    }

    public ProductDto getProduct(String productId) {
        UUID id = UUID.fromString(productId);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + productId));
        return productMapper.toDto(product);
    }

    @Transactional
    public ProductDto createProduct(ProductDto productDto) {
        Product product = productMapper.toEntity(productDto);
        if (product.getQuantityState() == null) {
            product.setQuantityState(ru.yandex.practicum.dto.QuantityState.ENDED);
        }
        product = productRepository.save(product);
        return productMapper.toDto(product);
    }

    @Transactional
    public ProductDto updateProduct(ProductDto productDto) {
        if (productDto.getProductId() == null) {
            throw new IllegalArgumentException("Product id must be provided for update");
        }
        Product existing = productRepository.findById(productDto.getProductId())
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + productDto.getProductId()));
        existing.setProductName(productDto.getProductName());
        existing.setDescription(productDto.getDescription());
        existing.setImageSrc(productDto.getImageSrc());
        existing.setProductCategory(productDto.getProductCategory());
        existing.setPrice(productDto.getPrice());
        return productMapper.toDto(productRepository.save(existing));
    }

    @Transactional
    public Boolean removeProduct(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + productId));
        product.setProductState(ProductState.DEACTIVATE);
        productRepository.save(product);
        return true;
    }

    @Transactional
    public Boolean setQuantityState(SetProductQuantityStateRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + request.getProductId()));
        product.setQuantityState(request.getQuantityState());
        productRepository.save(product);
        return true;
    }

}
