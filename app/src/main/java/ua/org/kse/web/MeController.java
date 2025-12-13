package ua.org.kse.web;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class MeController {
    @GetMapping("/me")
    public Map<String, Object> me(Authentication authentication) {
        OAuth2User user = (OAuth2User) authentication.getPrincipal();

        List<String> authorities = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .toList();

        return Map.of(
            "name", user.getName(),
            "attributes", user.getAttributes(),
            "authorities", authorities
        );
    }
}