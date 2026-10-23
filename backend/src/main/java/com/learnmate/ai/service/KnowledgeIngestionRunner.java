package com.learnmate.ai.service;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class KnowledgeIngestionRunner implements ApplicationRunner {

    private final RagService ragService;

    public KnowledgeIngestionRunner(RagService ragService) {
        this.ragService = ragService;
    }

    @Override
    public void run(ApplicationArguments args) {
        ragService.ingestIfEmpty();
    }
}
