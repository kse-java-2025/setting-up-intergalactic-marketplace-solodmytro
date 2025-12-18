package ua.org.kse.external;

import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import org.springframework.stereotype.Component;

@Component
public class CosmicTagValidator implements CosmicTagPolicy {
    private final CosmicDictionaryClient client;

    public CosmicTagValidator(CosmicDictionaryClient client) {
        this.client = client;
    }

    @Override
    public boolean isAllowed(String cosmicTag) {
        if (cosmicTag == null || cosmicTag.isBlank()) {
            return true;
        }

        String[] terms = client.fetchAllowedTerms();
        return isAllowedByTerms(cosmicTag, terms);
    }

    private boolean isAllowedByTerms(String tag, String[] terms) {
        if (terms == null || terms.length == 0) {
            return true;
        }

        String lowerTag = tag.toLowerCase(Locale.ROOT);
        return Arrays.stream(terms)
            .filter(Objects::nonNull)
            .map(t -> t.toLowerCase(Locale.ROOT))
            .anyMatch(lowerTag::contains);
    }
}