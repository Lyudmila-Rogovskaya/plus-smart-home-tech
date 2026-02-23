package ru.yandex.practicum.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.dto.DimensionDto;
import ru.yandex.practicum.dto.NewProductInWarehouseRequest;
import ru.yandex.practicum.entity.Dimension;
import ru.yandex.practicum.entity.WarehouseProduct;

@Component
public class WarehouseMapper {
    public WarehouseProduct toEntity(NewProductInWarehouseRequest request) {
        WarehouseProduct product = new WarehouseProduct();
        product.setProductId(request.getProductId());
        product.setFragile(request.getFragile());
        product.setWeight(request.getWeight());
        product.setQuantity(0L); // изначально 0
        if (request.getDimension() != null) {
            Dimension dim = new Dimension();
            dim.setWidth(request.getDimension().getWidth());
            dim.setHeight(request.getDimension().getHeight());
            dim.setDepth(request.getDimension().getDepth());
            product.setDimension(dim);
        }
        return product;
    }

    public DimensionDto toDto(Dimension dim) {
        if (dim == null) return null;
        DimensionDto dto = new DimensionDto();
        dto.setWidth(dim.getWidth());
        dto.setHeight(dim.getHeight());
        dto.setDepth(dim.getDepth());
        return dto;
    }

}
