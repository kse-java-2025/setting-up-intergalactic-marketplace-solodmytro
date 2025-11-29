package ua.org.kse.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.org.kse.domain.product.Category;
import ua.org.kse.domain.product.CosmicTag;
import ua.org.kse.domain.product.Product;
import ua.org.kse.dto.ProductCreateDto;
import ua.org.kse.dto.ProductDto;
import ua.org.kse.dto.ProductListDto;
import ua.org.kse.dto.ProductUpdateDto;
import ua.org.kse.error.BadRequestException;
import ua.org.kse.error.NotFoundException;
import ua.org.kse.external.CosmicDictionaryClient;
import ua.org.kse.external.TagServiceException;
import ua.org.kse.mapper.ProductMapper;
import ua.org.kse.repository.CategoryRepository;
import ua.org.kse.repository.ProductRepository;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductMapper mapper;
    private final CosmicDictionaryClient cosmicClient;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public ProductDto create(ProductCreateDto dto) {
        validateCosmicTag(dto.cosmicTag());

        Category category = resolveCategory(dto.category());

        Product domain = mapper.toDomain(dto);
        domain.setCategory(category);

        Product saved = productRepository.save(domain);
        return mapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDto getById(String id) {
        Long numericId = parseIdOrThrowNotFound(id);
        Product p = productRepository.findById(numericId)
            .orElseThrow(() -> new NotFoundException("Product with id " + id + " not found"));
        return mapper.toDto(p);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductListDto getAll(int page, int size) {
        if (size <= 0) {
            size = 10;
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<Product> result = productRepository.findAll(pageable);

        List<ProductDto> items = result.getContent().stream()
            .map(mapper::toDto)
            .toList();

        ProductListDto out = new ProductListDto();
        out.setItems(items);
        out.setPage(result.getNumber());
        out.setSize(result.getSize());
        out.setTotalItems(result.getTotalElements());

        int totalPages = result.getTotalPages();
        if (totalPages == 0) {
            totalPages = 1;
        }
        out.setTotalPages(totalPages);

        return out;
    }

    @Override
    @Transactional
    public ProductDto update(String id, ProductUpdateDto dto) {
        Long numericId = parseIdOrThrowNotFound(id);
        Product existing = productRepository.findById(numericId)
            .orElseThrow(() -> new NotFoundException("Product with id " + id + " not found"));

        validateCosmicTag(dto.getCosmicTag());

        if (dto.getCategory() != null) {
            Category category = resolveCategory(dto.getCategory());
            existing.setCategory(category);
        }

        mapper.updateDomain(dto, existing);

        if (dto.getCosmicTag() != null) {
            existing.setCosmicTag(new CosmicTag(dto.getCosmicTag()));
        }

        Product saved = productRepository.save(existing);
        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    public void delete(String id) {
        Long numericId;
        try {
            numericId = Long.valueOf(id);
        } catch (NumberFormatException ex) {
            return;
        }

        if (!productRepository.existsById(numericId)) {
            return;
        }

        productRepository.deleteById(numericId);
    }

    private Category resolveCategory(String categoryName) {
        if (categoryName == null || categoryName.isBlank()) {
            throw new BadRequestException("Category must be provided");
        }

        return categoryRepository.findByName(categoryName)
            .orElseGet(() -> categoryRepository.save(new Category(null, categoryName)));
    }

    private void validateCosmicTag(String cosmicTag) {
        if (cosmicTag == null) {
            return;
        }

        try {
            if (!cosmicClient.isAllowedTag(cosmicTag)) {
                throw new BadRequestException(
                    "cosmicTag '" + cosmicTag + "' is not allowed by external dictionary");
            }
        } catch (TagServiceException ex) {
            log.error("Failed to validate cosmicTag '{}' with external dictionary", cosmicTag, ex);
            throw ex;
        }
    }

    private Long parseIdOrThrowNotFound(String id) {
        try {
            return Long.valueOf(id);
        } catch (NumberFormatException ex) {
            throw new NotFoundException("Product with id " + id + " not found");
        }
    }
}