package ua.org.kse.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import ua.org.kse.domain.product.Category;
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
import ua.org.kse.mapper.ProductMapperImpl;
import ua.org.kse.repository.CategoryRepository;
import ua.org.kse.repository.ProductRepository;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {
    @Spy
    private ProductMapper mapper = new ProductMapperImpl();

    @Mock
    private CosmicDictionaryClient cosmicClient;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductServiceImpl service;

    private Map<Long, Product> productStore;
    private Map<String, Category> categoryStore;
    private AtomicLong productSeq;
    private AtomicLong categorySeq;

    @BeforeEach
    void setUp() {
        productStore = new ConcurrentHashMap<>();
        categoryStore = new ConcurrentHashMap<>();
        productSeq = new AtomicLong(0);
        categorySeq = new AtomicLong(0);

        lenient().when(cosmicClient.isAllowedTag(anyString())).thenReturn(true);

        lenient().when(categoryRepository.findByName(anyString()))
            .thenAnswer(invocation -> {
                String name = invocation.getArgument(0);
                return Optional.ofNullable(categoryStore.get(name));
            });

        lenient().when(categoryRepository.save(any(Category.class)))
            .thenAnswer(invocation -> {
                Category c = invocation.getArgument(0);
                if (c.getId() == null) {
                    c.setId(categorySeq.incrementAndGet());
                }
                categoryStore.put(c.getName(), c);
                return c;
            });

        lenient().when(productRepository.save(any(Product.class)))
            .thenAnswer(invocation -> {
                Product p = invocation.getArgument(0);
                if (p.getId() == null) {
                    p.setId(productSeq.incrementAndGet());
                }
                productStore.put(p.getId(), p);
                return p;
            });

        lenient().when(productRepository.findById(anyLong()))
            .thenAnswer(invocation -> {
                Long id = invocation.getArgument(0);
                return Optional.ofNullable(productStore.get(id));
            });

        lenient().when(productRepository.existsById(anyLong()))
            .thenAnswer(invocation -> {
                Long id = invocation.getArgument(0);
                return productStore.containsKey(id);
            });

        lenient().doAnswer(invocation -> {
            Long id = invocation.getArgument(0);
            productStore.remove(id);
            return null;
        }).when(productRepository).deleteById(anyLong());

        lenient().when(productRepository.findAll(any(Pageable.class)))
            .thenAnswer(invocation -> {
                Pageable pageable = invocation.getArgument(0);
                List<Product> sorted = productStore.values().stream()
                    .sorted(Comparator.comparing(Product::getName))
                    .toList();

                int start = (int) pageable.getOffset();
                int end = Math.min(start + pageable.getPageSize(), sorted.size());
                List<Product> content = start >= sorted.size() ? List.of() : sorted.subList(start, end);

                return new PageImpl<>(content, pageable, sorted.size());
            });
    }

    @Test
    void create_whenTagAllowed_savesAndReturnsDto() {
        ProductCreateDto dto = new ProductCreateDto(
            "Moon Cheese",
            "Cheese from the dark side",
            "Food",
            BigDecimal.valueOf(19.99),
            "star-delicacy"
        );

        ProductDto result = service.create(dto);

        assertThat(result.getId()).isNotNull();
        assertThat(result.getName()).isEqualTo("Moon Cheese");
        assertThat(result.getCategory()).isEqualTo("Food");
        assertThat(result.getCosmicTag()).isEqualTo("star-delicacy");
    }

    @Test
    void create_whenTagNotAllowed_throwsBadRequestException() {
        when(cosmicClient.isAllowedTag("boring-tag")).thenReturn(false);

        ProductCreateDto dto = new ProductCreateDto(
            "Bad Product",
            "Should fail",
            "Test",
            BigDecimal.valueOf(10.00),
            "boring-tag"
        );

        assertThrows(BadRequestException.class, () -> service.create(dto));
    }

    @Test
    void create_whenTagServiceFails_throwsTagServiceException() {
        when(cosmicClient.isAllowedTag("star-delicacy"))
            .thenThrow(new TagServiceException("boom", new RuntimeException()));

        ProductCreateDto dto = new ProductCreateDto(
            "Moon Cheese",
            "Cheese",
            "Food",
            BigDecimal.valueOf(19.99),
            "star-delicacy"
        );

        assertThrows(TagServiceException.class, () -> service.create(dto));
    }

    @Test
    void getById_whenExisting_returnsDto() {
        ProductCreateDto dto = new ProductCreateDto(
            "Nebula Ice Cream",
            "Cosmic dessert",
            "Dessert",
            BigDecimal.valueOf(9.99),
            "galaxy-cream"
        );

        ProductDto created = service.create(dto);

        ProductDto found = service.getById(created.getId());

        assertThat(found.getId()).isEqualTo(created.getId());
        assertThat(found.getName()).isEqualTo("Nebula Ice Cream");
    }

    @Test
    void getById_whenNotExisting_throwsNotFound() {
        assertThrows(NotFoundException.class, () -> service.getById("non-existing-id"));
    }

    @Test
    void getAll_returnsSortedAndPagedProducts() {
        ProductCreateDto dto1 = new ProductCreateDto(
            "Zeta Cola",
            "Space drink",
            "Drinks",
            BigDecimal.valueOf(2.50),
            "star-cola"
        );
        ProductCreateDto dto2 = new ProductCreateDto(
            "Asteroid Dust",
            "Spicy powder",
            "Spices",
            BigDecimal.valueOf(5.00),
            "galaxy-dust"
        );
        ProductCreateDto dto3 = new ProductCreateDto(
            "Comet Candy",
            "Sweet tail",
            "Sweets",
            BigDecimal.valueOf(3.00),
            "comet-candy"
        );

        service.create(dto1);
        service.create(dto2);
        service.create(dto3);

        ProductListDto page0 = service.getAll(0, 2);
        ProductListDto page1 = service.getAll(1, 2);
        ProductListDto farPage = service.getAll(10, 2);

        assertThat(page0.getItems()).hasSize(2);
        assertThat(page0.getTotalItems()).isEqualTo(3);
        assertThat(page0.getTotalPages()).isEqualTo(2);

        assertThat(page0.getItems().getFirst().getName()).isEqualTo("Asteroid Dust");
        assertThat(page0.getItems().get(1).getName()).isEqualTo("Comet Candy");

        assertThat(page1.getItems()).hasSize(1);
        assertThat(page1.getItems().getFirst().getName()).isEqualTo("Zeta Cola");

        assertThat(farPage.getItems()).isEmpty();
    }

    @Test
    void update_whenExisting_updatesFieldsAndTag() {
        ProductCreateDto createDto = new ProductCreateDto(
            "Moon Cheese",
            "Cheese from the dark side",
            "Food",
            BigDecimal.valueOf(19.99),
            "galaxy-delicacy"
        );

        ProductDto created = service.create(createDto);

        ProductUpdateDto updateDto = new ProductUpdateDto();
        updateDto.setName("Moon Cheese Deluxe");
        updateDto.setPrice(BigDecimal.valueOf(29.99));
        updateDto.setCosmicTag("star-premium");

        ProductDto updated = service.update(created.getId(), updateDto);

        assertThat(updated.getName()).isEqualTo("Moon Cheese Deluxe");
        assertThat(updated.getPrice()).isEqualTo(BigDecimal.valueOf(29.99));
        assertThat(updated.getCosmicTag()).isEqualTo("star-premium");
        assertThat(updated.getDescription()).isEqualTo("Cheese from the dark side");
        assertThat(updated.getCategory()).isEqualTo("Food");
    }

    @Test
    void update_whenNotExisting_throwsNotFound() {
        ProductUpdateDto dto = new ProductUpdateDto();
        dto.setCosmicTag("star-premium");

        assertThrows(NotFoundException.class, () -> service.update("missing-id", dto));
    }

    @Test
    void update_whenTagNotAllowed_throwsBadRequestException() {
        ProductCreateDto createDto = new ProductCreateDto(
            "Moon Cheese",
            "Cheese",
            "Food",
            BigDecimal.valueOf(19.99),
            "star-delicacy"
        );
        ProductDto created = service.create(createDto);

        ProductUpdateDto updateDto = new ProductUpdateDto();
        updateDto.setCosmicTag("boring-tag");

        when(cosmicClient.isAllowedTag("boring-tag")).thenReturn(false);

        String id = created.getId();

        assertThrows(BadRequestException.class, () -> service.update(id, updateDto));
    }

    @Test
    void delete_removesProduct() {
        ProductCreateDto createDto = new ProductCreateDto(
            "Moon Cheese",
            "Cheese",
            "Food",
            BigDecimal.valueOf(19.99),
            "star-delicacy"
        );
        ProductDto created = service.create(createDto);

        service.delete(created.getId());

        String id = created.getId();
        assertThrows(NotFoundException.class, () -> service.getById(id));
    }

    @Test
    void getAll_whenEmptyStore_returnsEmptyList() {
        ProductListDto list = service.getAll(0, 10);

        assertThat(list.getItems()).isEmpty();
        assertThat(list.getTotalItems()).isZero();
        assertThat(list.getTotalPages()).isEqualTo(1);
    }
}