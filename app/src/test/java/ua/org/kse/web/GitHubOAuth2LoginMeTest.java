package ua.org.kse.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ua.org.kse.config.SecurityConfig;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MeController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
class GitHubOAuth2LoginMeTest {
    @Autowired
    MockMvc mockMvc;

    @Test
    void me_withOauth2Login_returns200AndUserInfo() throws Exception {
        mockMvc.perform(get("/me")
                .with(oauth2Login().attributes(a -> a.put("login", "octocat"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.attributes.login").value("octocat"));
    }
}