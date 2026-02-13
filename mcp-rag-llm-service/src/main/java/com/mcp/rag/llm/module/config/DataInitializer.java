package com.mcp.rag.llm.module.config;

import com.mcp.rag.llm.module.entity.Iab;
import com.mcp.rag.llm.module.service.IabCategoriesService;
import com.opencsv.CSVReader;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class DataInitializer implements CommandLineRunner {
    
    private final IabCategoriesService iabService;
    private final ResourceLoader resourceLoader;

    private final VectorStore vectorStore;

    @Value("${dataFileName:iab.csv}")
    private String dataFileName;
    
    @Autowired
    public DataInitializer(IabCategoriesService iabService, ResourceLoader resourceLoader, VectorStore vectorStore) {
        this.iabService = iabService;
        this.resourceLoader = resourceLoader;
        this.vectorStore = vectorStore;
    }
    
    @Override
    public void run(String... args) throws Exception {
        // Check if iabs already exist to avoid duplicates
        if (iabService.getAllIabs().size() == 0) {
           // initializeSampleIabs();
            loadInitialData();
        }
    }

    private void loadInitialData(){
        // Path matches src/main/resources/data.csv

        Resource resource = resourceLoader.getResource("classpath:" + dataFileName);

        List<Iab> list = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new InputStreamReader(resource.getInputStream()))) {
            List<String[]> allRows = reader.readAll();

            // Skip header and process rows
            for (int i = 1; i < allRows.size(); i++) {
                String[] row = allRows.get(i);

                // Get values by index
                Long id = null;
                Long parentId = null;
                String name = null;
                String tier1 = null;
                String tier2 = null;
                String tier3 = null;
                String tier4 = null;

                if(row[0].trim().length() != 0){
                    id = Long.valueOf(row[0].trim());
                }
                if(row[1].trim().length() != 0){
                    parentId = Long.valueOf(row[1].trim());
                }
                if(row[2].trim().length() != 0){
                    name = row[2].trim();
                }
                if(row[3].trim().length() != 0){
                    tier1 = row[3].trim();
                }
                if(row[4].trim().length() != 0){
                    tier2 = row[4].trim();
                }
                if(row[5].trim().length() != 0){
                    tier3 = row[5].trim();
                }
                if(row[6].trim().length() != 0){
                    tier4 = row[6].trim();
                }
                Iab iab = new Iab (id, parentId, name, tier1, tier2, tier3, tier4);
                iabService.addIab(iab);
                list.add(iab);
            }
            embedAndStoreData(list);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load CSV: " + e.getMessage());
        }
    }

    public String generateEmbeddingText(Iab iab) {
        // Collect tiers into a list to filter out nulls easily
        List<String> tierPath = Stream.of(iab.getTier1(), iab.getTier2(), iab.getTier3(), iab.getTier4())
                .filter(Objects::nonNull)
                .filter(t -> !t.isBlank())
                .collect(Collectors.toList());

        String path = String.join(" > ", tierPath);
        String parentInfo = (iab.getParentId() != null) ? "Under Parent ID: " + iab.getParentId() : "Root";

        // Format: Name (ID: 123, Parent: 45) Taxonomy: Tier1 > Tier2...
        return String.format("Taxonomy/IAB has name %s (ID: %s, %s) Taxonomy Hierarchy: %s",
                iab.getName(), iab.getId(), parentInfo, path);
    }

    public void embedAndStoreData(List<Iab> iabList) {
        List<Document> documents = iabList.stream().map(iab -> {
            // 1. Create the semantic text for the embedding model
            String content = generateEmbeddingText(iab);

            // 2. Store original fields as metadata for filtering later
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("id", iab.getId());
            metadata.put("name", iab.getName());
            if(iab.getParentId() != null)
                metadata.put("parentId", iab.getParentId());
            if(iab.getTier1() != null)
                metadata.put("tier1", iab.getTier1());
            if(iab.getTier2() != null)
                metadata.put("tier2", iab.getTier2());
            if(iab.getTier3() != null)
                metadata.put("tier3", iab.getTier3());
            if(iab.getTier4() != null)
                metadata.put("tier4", iab.getTier4());
            // Add other tiers if needed for filtering

            return new Document(content, metadata);
        }).collect(Collectors.toList());

        vectorStore.add(documents);
    }
}
