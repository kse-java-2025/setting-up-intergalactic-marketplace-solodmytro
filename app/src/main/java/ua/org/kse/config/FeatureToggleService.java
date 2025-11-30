package ua.org.kse.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Getter
@Service
public class FeatureToggleService {
    private final boolean cosmoCatsEnabled;
    private final boolean kittyProductsEnabled;

    public FeatureToggleService(
        @Value("${feature.cosmoCats.enabled:true}") boolean cosmoCatsEnabled,
        @Value("${feature.kittyProducts.enabled:false}") boolean kittyProductsEnabled
    ) {
        this.cosmoCatsEnabled = cosmoCatsEnabled;
        this.kittyProductsEnabled = kittyProductsEnabled;
    }

}