package ua.org.kse.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ua.org.kse.domain.product.Product;
import ua.org.kse.dto.ProductCreateDto;
import ua.org.kse.dto.ProductDto;
import ua.org.kse.dto.ProductUpdateDto;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    @Mapping(source = "category.name", target = "category")
    @Mapping(source = "cosmicTag.value", target = "cosmicTag")
    ProductDto toDto(Product product);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category.name", source = "category")
    @Mapping(
        target = "cosmicTag",
        expression = "java(dto.cosmicTag() == null ? null : new CosmicTag(dto.cosmicTag()))"
    )
    Product toDomain(ProductCreateDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category.name", source = "category")
    @Mapping(target = "cosmicTag", ignore = true)
    void updateDomain(ProductUpdateDto dto, @MappingTarget Product product);
}