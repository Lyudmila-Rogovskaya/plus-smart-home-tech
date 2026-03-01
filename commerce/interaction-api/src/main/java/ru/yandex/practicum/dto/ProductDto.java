package ru.yandex.practicum.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class ProductDto {

    private UUID productId;
    private String productName;
    private String description;
    private String imageSrc;
    private QuantityState quantityState;
    private ProductState productState;
    private ProductCategory productCategory;
    private Double price;

}
