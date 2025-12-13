package ua.org.kse.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ua.org.kse.config.SecurityConfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = MeController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
class GitHubOAuthLoginTest {
    @Autowired
    MockMvc mockMvc;

    @Test
    void oauth2AuthorizationEndpoint_forGithub_redirectsToGithub() throws Exception {
        var result = mockMvc.perform(get("/oauth2/authorization/github"))
            .andExpect(status().is3xxRedirection())
            .andReturn();

        String location = result.getResponse().getHeader("Location");

        assertThat(location)
            .isNotBlank()
            .contains("github.com")
            .contains("/login/oauth/authorize");
    }
}