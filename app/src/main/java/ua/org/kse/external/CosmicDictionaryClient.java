package ua.org.kse.external;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class CosmicDictionaryClient {
    private final RestClient client;
    private final CosmicDictionaryMapper mapper;

    public CosmicDictionaryClient(RestClient cosmicRestClient, CosmicDictionaryMapper mapper) {
        this.client = cosmicRestClient;
        this.mapper = mapper;
    }

    public boolean isAllowedTag(String tag) {
        if (tag == null || tag.isBlank()) {
            return true;
        }

        String[] body;
        try {
            body = client.get()
                .uri("/api/terms")
                .retrieve()
                .body(String[].class);
        } catch (Exception ex) {
            throw new TagServiceException("Failed to fetch allowed cosmic tags", ex);
        }

        return mapper.isTagAllowed(tag, body);
    }
}