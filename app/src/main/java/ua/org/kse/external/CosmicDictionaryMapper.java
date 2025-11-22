package ua.org.kse.external;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Locale;

@Component
public class CosmicDictionaryMapper {
    public boolean isTagAllowed(String tag, String[] terms) {
        if (terms == null) {
            return true;
        }

        String lower = tag.toLowerCase(Locale.ROOT);

        return Arrays.stream(terms)
            .anyMatch(t -> lower.contains(t.toLowerCase(Locale.ROOT)));
    }
}