package com.viktor.mono.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.viktor.mono.entity.Mcc;
import com.viktor.mono.repository.MccRepository;
import jakarta.annotation.PostConstruct;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;

@Service
public class MccService {

    private final MccRepository repository;

    public MccService(MccRepository repository) {
        this.repository = repository;
    }

    @PostConstruct
    public void fillDatabaseFromFile() {
        List<Mcc> localMccs = fromLocalFile();
        repository.truncate();
        repository.saveAll(localMccs);
    }

    public String mccCodeToDescription(Long mcc) {
        Mcc found = repository.findByCode(mcc);
        if (found == null) {
            return "Не відомо";
        }
        return found.getDescription();
    }

    @SneakyThrows
    private List<Mcc> fromLocalFile() {
        String json = readJsonFromFile();
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, new TypeReference<List<Mcc>>() {
        });
    }

    @SneakyThrows
    private String readJsonFromFile() {
        try (InputStream is = MccService.class.getClassLoader().getResourceAsStream("mcc.json")) {
            return new String(is.readAllBytes());
        }
    }
}
