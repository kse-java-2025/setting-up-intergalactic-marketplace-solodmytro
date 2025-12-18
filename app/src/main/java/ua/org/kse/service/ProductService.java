package ua.org.kse.service;

import ua.org.kse.dto.ProductCreateDto;
import ua.org.kse.dto.ProductDto;
import ua.org.kse.dto.ProductListDto;
import ua.org.kse.dto.ProductUpdateDto;

public interface ProductService {
    ProductDto createProduct(ProductCreateDto dto);

    ProductDto getProductById(String id);

    ProductListDto getProducts(int page, int size);

    ProductDto updateProduct(String id, ProductUpdateDto dto);

    void deleteProduct(String id);
}