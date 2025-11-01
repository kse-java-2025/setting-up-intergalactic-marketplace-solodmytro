package ua.org.kse.service;

import ua.org.kse.dto.ProductCreateDto;
import ua.org.kse.dto.ProductDto;
import ua.org.kse.dto.ProductListDto;
import ua.org.kse.dto.ProductUpdateDto;

public interface ProductService {
    ProductDto create(ProductCreateDto dto);

    ProductDto getById(String id);

    ProductListDto getAll(int page, int size);

    ProductDto update(String id, ProductUpdateDto dto);

    void delete(String id);
}