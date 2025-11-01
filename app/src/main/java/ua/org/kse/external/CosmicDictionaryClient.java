package ua.org.kse.external;

import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Component
public class CosmicDictionaryClient {
    private final RestClient client;

    public CosmicDictionaryClient(RestClient cosmicRestClient) {
        this.client = cosmicRestClient;
    }

    public boolean isAllowedTag(String tag) {
        if (tag == null || tag.isBlank()) {
            return true;
        }

        try {
            String[] body = client.get()
                .uri("/api/terms")
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                })
                .body(String[].class);

            if (body == null) {
                return true;
            }

            Set<String> allowed = new HashSet<>(Arrays.asList(body));
            String lower = tag.toLowerCase(Locale.ROOT);
            return allowed.stream().anyMatch(t -> lower.contains(t.toLowerCase(Locale.ROOT)));
        } catch (Exception ignored) {
            return true;
        }
    }
}