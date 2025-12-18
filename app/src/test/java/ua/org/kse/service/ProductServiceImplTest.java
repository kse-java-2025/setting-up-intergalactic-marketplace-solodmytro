package ua.org.kse.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.org.kse.dto.ProductCreateDto;
import ua.org.kse.dto.ProductDto;
import ua.org.kse.dto.ProductListDto;
import ua.org.kse.dto.ProductUpdateDto;
import ua.org.kse.error.CosmicTagNotAllowedException;
import ua.org.kse.error.ProductNotFoundException;
import ua.org.kse.external.CosmicTagPolicy;
import ua.org.kse.external.TagServiceException;
import ua.org.kse.mapper.ProductMapper;
import ua.org.kse.mapper.ProductMapperImpl;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {
    @Spy
    private ProductMapper mapper = new ProductMapperImpl();

    @Mock
    private CosmicTagPolicy cosmicTagPolicy;

    @InjectMocks
    private ProductServiceImpl service;

    @BeforeEach
    void setUp() {
        lenient().when(cosmicTagPolicy.isAllowed(anyString())).thenReturn(true);
    }

    @Test
    void create_Product_whenTagAllowed_savesAndReturnsDto() {
        ProductCreateDto dto = new ProductCreateDto(
            "Moon Cheese",
            "Cheese from the dark side",
            "Food",
            BigDecimal.valueOf(19.99),
            "star-delicacy"
        );

        ProductDto result = service.createProduct(dto);

        assertThat(result.getId()).isNotNull();
        assertThat(result.getName()).isEqualTo("Moon Cheese");
        assertThat(result.getCategory()).isEqualTo("Food");
        assertThat(result.getCosmicTag()).isEqualTo("star-delicacy");
    }

    @Test
    void create_Product_whenTagNotAllowed_throwsCosmicTagNotAllowedException() {
        when(cosmicTagPolicy.isAllowed("boring-tag")).thenReturn(false);

        ProductCreateDto dto = new ProductCreateDto(
            "Bad Product",
            "Should fail",
            "Test",
            BigDecimal.valueOf(10.00),
            "boring-tag"
        );

        assertThrows(CosmicTagNotAllowedException.class, () -> service.createProduct(dto));
    }

    @Test
    void create_Product_whenTagServiceFails_throwsTagServiceException() {
        when(cosmicTagPolicy.isAllowed("star-delicacy"))
            .thenThrow(new TagServiceException(new RuntimeException("boom")));

        ProductCreateDto dto = new ProductCreateDto(
            "Moon Cheese",
            "Cheese",
            "Food",
            BigDecimal.valueOf(19.99),
            "star-delicacy"
        );

        assertThrows(TagServiceException.class, () -> service.createProduct(dto));
    }

    @Test
    void getProductById_whenExisting_returnsDto() {
        ProductCreateDto dto = new ProductCreateDto(
            "Nebula Ice Cream",
            "Cosmic dessert",
            "Dessert",
            BigDecimal.valueOf(9.99),
            "galaxy-cream"
        );

        ProductDto created = service.createProduct(dto);

        ProductDto found = service.getProductById(created.getId());

        assertThat(found.getId()).isEqualTo(created.getId());
        assertThat(found.getName()).isEqualTo("Nebula Ice Cream");
    }

    @Test
    void getProductById_whenNotExisting_throwsNotFound() {
        assertThrows(ProductNotFoundException.class, () -> service.getProductById("non-existing-id"));
    }

    @Test
    void getProducts_returnsSortedAndPagedProducts() {
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

        service.createProduct(dto1);
        service.createProduct(dto2);
        service.createProduct(dto3);

        ProductListDto page0 = service.getProducts(0, 2);
        ProductListDto page1 = service.getProducts(1, 2);
        ProductListDto farPage = service.getProducts(10, 2);

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
    void update_Product_whenExisting_updatesFieldsAndTag() {
        ProductCreateDto createDto = new ProductCreateDto(
            "Moon Cheese",
            "Cheese from the dark side",
            "Food",
            BigDecimal.valueOf(19.99),
            "galaxy-delicacy"
        );

        ProductDto created = service.createProduct(createDto);

        ProductUpdateDto updateDto = new ProductUpdateDto();
        updateDto.setName("Moon Cheese Deluxe");
        updateDto.setPrice(BigDecimal.valueOf(29.99));
        updateDto.setCosmicTag("star-premium");

        ProductDto updated = service.updateProduct(created.getId(), updateDto);

        assertThat(updated.getName()).isEqualTo("Moon Cheese Deluxe");
        assertThat(updated.getPrice()).isEqualTo(BigDecimal.valueOf(29.99));
        assertThat(updated.getCosmicTag()).isEqualTo("star-premium");
        assertThat(updated.getDescription()).isEqualTo("Cheese from the dark side");
        assertThat(updated.getCategory()).isEqualTo("Food");
    }

    @Test
    void update_Product_whenNotExisting_throwsNotFound() {
        ProductUpdateDto dto = new ProductUpdateDto();
        dto.setCosmicTag("star-premium");

        assertThrows(ProductNotFoundException.class, () -> service.updateProduct("missing-id", dto));
    }

    @Test
    void update_Product_whenTagNotAllowed_throwsCosmicTagNotAllowedException() {
        ProductCreateDto createDto = new ProductCreateDto(
            "Moon Cheese",
            "Cheese",
            "Food",
            BigDecimal.valueOf(19.99),
            "star-delicacy"
        );
        ProductDto created = service.createProduct(createDto);

        ProductUpdateDto updateDto = new ProductUpdateDto();
        updateDto.setCosmicTag("boring-tag");

        when(cosmicTagPolicy.isAllowed("boring-tag")).thenReturn(false);

        String id = created.getId();

        assertThrows(CosmicTagNotAllowedException.class, () -> service.updateProduct(id, updateDto));
    }

    @Test
    void delete_Product_removesProduct() {
        ProductCreateDto createDto = new ProductCreateDto(
            "Moon Cheese",
            "Cheese",
            "Food",
            BigDecimal.valueOf(19.99),
            "star-delicacy"
        );
        ProductDto created = service.createProduct(createDto);

        service.deleteProduct(created.getId());

        String id = created.getId();
        assertThrows(ProductNotFoundException.class, () -> service.getProductById(id));
    }

    @Test
    void getProducts_whenEmptyStore_returnsEmptyList() {
        ProductListDto list = service.getProducts(0, 10);

        assertThat(list.getItems()).isEmpty();
        assertThat(list.getTotalItems()).isZero();
        assertThat(list.getTotalPages()).isEqualTo(1);
    }
}