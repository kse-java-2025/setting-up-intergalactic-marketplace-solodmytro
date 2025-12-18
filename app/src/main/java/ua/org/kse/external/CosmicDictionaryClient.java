package ua.org.kse.external;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import ua.org.kse.config.CosmoExternalProperties;

@Service
public class CosmicDictionaryClient {
    private final RestClient client;
    private final CosmoExternalProperties props;

    public CosmicDictionaryClient(RestClient cosmicRestClient, CosmoExternalProperties props) {
        this.client = cosmicRestClient;
        this.props = props;
    }

    public String[] fetchAllowedTerms() {
        String path = (props.termsPath() == null || props.termsPath().isBlank())
            ? "/api/terms"
            : props.termsPath();

        try {
            return client.get()
                .uri(path)
                .retrieve()
                .body(String[].class);
        } catch (Exception ex) {
            throw new TagServiceException(ex);
        }
    }
}