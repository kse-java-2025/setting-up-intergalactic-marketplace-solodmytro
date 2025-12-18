package ua.org.kse.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ua.org.kse.dto.ProductCreateDto;
import ua.org.kse.dto.ProductDto;
import ua.org.kse.dto.ProductListDto;
import ua.org.kse.dto.ProductUpdateDto;
import ua.org.kse.service.ProductService;
import ua.org.kse.web.error.GlobalExceptionHandler;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@Import(GlobalExceptionHandler.class)
class ProductControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService service;

    @Test
    void create_whenValid_returns201WithBody() throws Exception {
        ProductDto dto = new ProductDto();
        dto.setId("123");
        dto.setName("Moon Cheese");
        dto.setDescription("Cheese imported from the dark side of the moon");
        dto.setCategory("Food");
        dto.setPrice(BigDecimal.valueOf(19.99));
        dto.setCosmicTag("star-delicacy");

        when(service.createProduct(any(ProductCreateDto.class))).thenReturn(dto);

        String body = """
            {
              "name": "Moon Cheese",
              "description": "Cheese imported from the dark side of the moon",
              "category": "Food",
              "price": 19.99,
              "cosmicTag": "star-delicacy"
            }
            """;

        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id", is("123")))
            .andExpect(jsonPath("$.name", is("Moon Cheese")))
            .andExpect(jsonPath("$.category", is("Food")))
            .andExpect(jsonPath("$.cosmicTag", is("star-delicacy")));
    }

    @Test
    void create_whenInvalidBody_returnsValidationError() throws Exception {
        String body = """
            {
              "name": "Bad Product",
              "description": "Should fail",
              "category": "Test",
              "price": 0,
              "cosmicTag": null
            }
            """;

        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.type", is("https://example.com/problems/validation-error")))
            .andExpect(jsonPath("$.errors", hasSize(2)));

        verifyNoInteractions(service);
    }

    @Test
    void update_whenInvalidCosmicTag_returnsValidationError() throws Exception {
        String body = """
            {
              "cosmicTag": "boring-cheese"
            }
            """;

        mockMvc.perform(put("/api/v1/products/{id}", "some-id")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.type", is("https://example.com/problems/validation-error")))
            .andExpect(jsonPath("$.errors[0].field", is("cosmicTag")));

        verifyNoInteractions(service);
    }

    @Test
    void getAll_whenInvalidSize_returnsConstraintViolationError() throws Exception {
        mockMvc.perform(get("/api/v1/products")
                .param("page", "0")
                .param("size", "0"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.type", is("https://example.com/problems/validation-error")))
            .andExpect(jsonPath("$.errors[0].field", is("getAll.size")));

        verifyNoInteractions(service);
    }

    @Test
    void getAll_whenValid_returnsList() throws Exception {
        ProductDto dto = new ProductDto();
        dto.setId("123");
        dto.setName("Moon Cheese");
        dto.setDescription("Cheese imported from the dark side of the moon");
        dto.setCategory("Food");
        dto.setPrice(BigDecimal.valueOf(19.99));
        dto.setCosmicTag("galaxy-delicacy");

        ProductListDto list = new ProductListDto();
        list.setItems(List.of(dto));
        list.setPage(0);
        list.setSize(10);
        list.setTotalItems(1);
        list.setTotalPages(1);

        when(service.getProducts(0, 10)).thenReturn(list);

        mockMvc.perform(get("/api/v1/products")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items", hasSize(1)))
            .andExpect(jsonPath("$.items[0].id", is("123")))
            .andExpect(jsonPath("$.page", is(0)))
            .andExpect(jsonPath("$.size", is(10)))
            .andExpect(jsonPath("$.totalItems", is(1)))
            .andExpect(jsonPath("$.totalPages", is(1)));
    }

    @Test
    void update_whenValid_callsServiceAndReturnsDto() throws Exception {
        ProductDto dto = new ProductDto();
        dto.setId("123");
        dto.setName("Moon Cheese Deluxe");
        dto.setDescription("Cheese imported from the dark side of the moon");
        dto.setCategory("Food");
        dto.setPrice(BigDecimal.valueOf(29.99));
        dto.setCosmicTag("star-premium");

        when(service.updateProduct(eq("123"), any(ProductUpdateDto.class))).thenReturn(dto);

        String body = """
            {
              "name": "Moon Cheese Deluxe",
              "price": 29.99,
              "cosmicTag": "star-premium"
            }
            """;

        mockMvc.perform(put("/api/v1/products/{id}", "123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is("123")))
            .andExpect(jsonPath("$.name", is("Moon Cheese Deluxe")))
            .andExpect(jsonPath("$.price", is(29.99)))
            .andExpect(jsonPath("$.cosmicTag", is("star-premium")));
    }

    @Test
    void delete_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/products/{id}", "123"))
            .andExpect(status().isNoContent());
    }
}