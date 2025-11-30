package ua.org.kse.service;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CosmoCatService {
    public List<String> getCosmoCats() {
        return List.of("Luna", "Orion", "Nebula");
    }
}