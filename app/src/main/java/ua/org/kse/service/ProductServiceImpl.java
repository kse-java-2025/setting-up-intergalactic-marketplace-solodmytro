package ua.org.kse.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ua.org.kse.domain.product.CosmicTag;
import ua.org.kse.domain.product.Product;
import ua.org.kse.dto.ProductCreateDto;
import ua.org.kse.dto.ProductDto;
import ua.org.kse.dto.ProductListDto;
import ua.org.kse.dto.ProductUpdateDto;
import ua.org.kse.error.CosmicTagNotAllowedException;
import ua.org.kse.error.ProductNotFoundException;
import ua.org.kse.external.CosmicDictionaryClient;
import ua.org.kse.external.TagServiceException;
import ua.org.kse.mapper.ProductMapper;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductMapper mapper;
    private final CosmicDictionaryClient cosmicClient;
    private final Map<String, Product> store = new ConcurrentHashMap<>();

    @Override
    public ProductDto create(ProductCreateDto dto) {
        validateCosmicTag(dto.cosmicTag());

        Product domain = mapper.toDomain(dto);
        String id = UUID.randomUUID().toString();
        domain.setId(id);
        store.put(id, domain);
        return mapper.toDto(domain);
    }

    @Override
    public ProductDto getById(String id) {
        Product p = getExistingProductOrThrow(id);
        return mapper.toDto(p);
    }

    @Override
    public ProductListDto getAll(int page, int size) {
        List<Product> all = store.values().stream()
            .sorted(Comparator.comparing(Product::getName, Comparator.nullsLast(String::compareTo)))
            .toList();

        int totalItems = all.size();

        long startLong = Math.clamp((long) page * size, 0L, totalItems);
        long endLong = Math.clamp(startLong + size, startLong, totalItems);

        int start = (int) startLong;
        int end = (int) endLong;

        List<Product> slice = all.subList(start, end);

        ProductListDto out = new ProductListDto();
        out.setItems(slice.stream().map(mapper::toDto).toList());
        out.setPage(page);
        out.setSize(size);
        out.setTotalItems(totalItems);

        int totalPages;
        if (size <= 0) {
            totalPages = 1;
        } else {
            totalPages = Math.max(1, (int) Math.ceil((double) totalItems / size));
        }
        out.setTotalPages(totalPages);

        return out;
    }

    @Override
    public ProductDto update(String id, ProductUpdateDto dto) {
        Product existing = getExistingProductOrThrow(id);

        validateCosmicTag(dto.getCosmicTag());

        mapper.updateDomain(dto, existing);

        if (dto.getCosmicTag() != null) {
            existing.setCosmicTag(new CosmicTag(dto.getCosmicTag()));
        }

        store.put(id, existing);
        return mapper.toDto(existing);
    }

    @Override
    public void delete(String id) {
        store.remove(id);
    }

    private Product getExistingProductOrThrow(String id) {
        Product existing = store.get(id);
        if (existing == null) {
            throw new ProductNotFoundException(id);
        }
        return existing;
    }

    private void validateCosmicTag(String cosmicTag) {
        if (cosmicTag == null) {
            return;
        }

        try {
            if (!cosmicClient.isAllowedTag(cosmicTag)) {
                throw new CosmicTagNotAllowedException(cosmicTag);
            }
        } catch (TagServiceException ex) {
            log.error("Failed to validate cosmicTag '{}' with external dictionary", cosmicTag, ex);
            throw ex;
        }
    }
}