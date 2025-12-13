package ua.org.kse.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ua.org.kse.config.SecurityConfig;
import ua.org.kse.dto.ProductDto;
import ua.org.kse.dto.ProductListDto;
import ua.org.kse.service.ProductService;
import ua.org.kse.web.error.GlobalExceptionHandler;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
@ActiveProfiles("test")
class ProductControllerSecurityTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService service;

    @Test
    void getAll_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/products"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void getAll_withJwt_returns200() throws Exception {
        ProductDto dto = new ProductDto();
        dto.setId("1");
        dto.setName("Moon Cheese");
        dto.setCategory("Food");
        dto.setPrice(BigDecimal.valueOf(19.99));
        dto.setCosmicTag("star-delicacy");

        ProductListDto list = new ProductListDto();
        list.setItems(List.of(dto));
        list.setPage(0);
        list.setSize(10);
        list.setTotalItems(1);
        list.setTotalPages(1);

        when(service.getAll(0, 10)).thenReturn(list);

        mockMvc.perform(get("/api/v1/products")
                .with(jwt().jwt(j -> j
                    .issuer("http://localhost:8089")
                    .claim("scope", "products:read")
                )))
            .andExpect(status().isOk());
    }
}