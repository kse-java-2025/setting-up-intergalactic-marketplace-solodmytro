package ua.org.kse.service;

import org.springframework.stereotype.Service;

import java.util.List;
import ua.org.kse.config.FeatureGuard;

@Service
public class CosmoCatService {
    @FeatureGuard("cosmoCats")
    public List<String> getCosmoCats() {
        return List.of("Luna", "Orion", "Nebula");
    }
}