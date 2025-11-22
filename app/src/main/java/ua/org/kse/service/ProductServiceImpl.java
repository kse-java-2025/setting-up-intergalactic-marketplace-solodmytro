package ua.org.kse.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ua.org.kse.domain.product.Product;
import ua.org.kse.dto.ProductCreateDto;
import ua.org.kse.dto.ProductDto;
import ua.org.kse.dto.ProductListDto;
import ua.org.kse.dto.ProductUpdateDto;
import ua.org.kse.external.CosmicDictionaryClient;
import ua.org.kse.mapper.ProductMapper;
import ua.org.kse.web.error.BadRequestException;
import ua.org.kse.web.error.NotFoundException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductMapper mapper;
    private final CosmicDictionaryClient cosmicClient;
    private final Map<String, Product> store = new ConcurrentHashMap<>();

    @Override
    public ProductDto create(ProductCreateDto dto) {
        if (dto.cosmicTag() != null && !cosmicClient.isAllowedTag(dto.cosmicTag())) {
            throw new BadRequestException(
                "cosmicTag '" + dto.cosmicTag() + "' is not allowed by external dictionary");
        }

        Product domain = mapper.toDomain(dto);
        String id = UUID.randomUUID().toString();
        domain.setId(id);
        store.put(id, domain);
        return mapper.toDto(domain);
    }

    @Override
    public ProductDto getById(String id) {
        Product p = store.get(id);
        if (p == null) {
            throw new NotFoundException("Product with id " + id + " not found");
        }

        return mapper.toDto(p);
    }

    @Override
    public ProductListDto getAll(int page, int size) {
        List<Product> all = new ArrayList<>(store.values())
            .stream()
            .sorted(Comparator.comparing(Product::getName, Comparator.nullsLast(String::compareTo)))
            .toList();

        int totalItems = all.size();
        int from = Math.max(0, Math.min(page * size, totalItems));
        int to = Math.max(from, Math.min(from + size, totalItems));
        List<Product> slice = all.subList(from, to);

        ProductListDto out = new ProductListDto();
        out.setItems(slice.stream().map(mapper::toDto).toList());
        out.setPage(page);
        out.setSize(size);
        out.setTotalItems(totalItems);
        out.setTotalPages(size == 0 ? 1 : (int) Math.ceil((double) totalItems / size));
        return out;
    }

    @Override
    public ProductDto update(String id, ProductUpdateDto dto) {
        Product existing = store.get(id);
        if (existing == null) {
            throw new NotFoundException("Product with id " + id + " not found");
        }

        if (dto.getCosmicTag() != null && !cosmicClient.isAllowedTag(dto.getCosmicTag())) {
            throw new BadRequestException(
                "cosmicTag '" + dto.getCosmicTag() + "' is not allowed by external dictionary");
        }

        mapper.updateDomain(dto, existing);
        store.put(id, existing);
        return mapper.toDto(existing);
    }

    @Override
    public void delete(String id) {
        store.remove(id);
    }
}