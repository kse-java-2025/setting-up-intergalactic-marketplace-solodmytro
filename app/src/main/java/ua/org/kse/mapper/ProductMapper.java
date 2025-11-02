package ua.org.kse.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
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
    @Mapping(target = "category", expression = "java(dto.getCategory() == null ? null : new Category(dto.getCategory()))")
    @Mapping(target = "cosmicTag", expression = "java(new CosmicTag(dto.getCosmicTag()))")
    Product toDomain(ProductCreateDto dto);

    @Mapping(target = "category", expression = "java(dto.getCategory() == null ? product.getCategory() : new Category(dto.getCategory()))")
    @Mapping(target = "cosmicTag", expression = "java(dto.getCosmicTag() == null ? product.getCosmicTag() : new CosmicTag(dto.getCosmicTag()))")
    void updateDomain(ProductUpdateDto dto, @MappingTarget Product product);
}