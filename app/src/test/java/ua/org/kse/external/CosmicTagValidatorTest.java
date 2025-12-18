package ua.org.kse.external;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CosmicTagValidatorTest {
    @Test
    void isAllowed_whenTagIsNull_returnsTrue() {
        CosmicDictionaryClient client = mock(CosmicDictionaryClient.class);
        CosmicTagValidator validator = new CosmicTagValidator(client);

        boolean allowed = validator.isAllowed(null);

        assertThat(allowed).isTrue();
    }

    @Test
    void isAllowed_whenTermsEmpty_returnsTrue() {
        CosmicDictionaryClient client = mock(CosmicDictionaryClient.class);
        when(client.fetchAllowedTerms()).thenReturn(new String[0]);

        CosmicTagValidator validator = new CosmicTagValidator(client);

        boolean allowed = validator.isAllowed("anything");

        assertThat(allowed).isTrue();
    }

    @Test
    void isAllowed_whenTermMatches_returnsTrue() {
        CosmicDictionaryClient client = mock(CosmicDictionaryClient.class);
        when(client.fetchAllowedTerms()).thenReturn(new String[] {"star", "galaxy"});

        CosmicTagValidator validator = new CosmicTagValidator(client);

        boolean allowed = validator.isAllowed("star-delicacy");

        assertThat(allowed).isTrue();
    }

    @Test
    void isAllowed_whenNoTermMatches_returnsFalse() {
        CosmicDictionaryClient client = mock(CosmicDictionaryClient.class);
        when(client.fetchAllowedTerms()).thenReturn(new String[] {"star", "galaxy"});

        CosmicTagValidator validator = new CosmicTagValidator(client);

        boolean allowed = validator.isAllowed("boring-cheese");

        assertThat(allowed).isFalse();
    }
}