package com.mcp.rag.llm.module.tool;

import com.mcp.rag.llm.module.entity.Iab;
import com.mcp.rag.llm.module.service.CacheService;
import com.mcp.rag.llm.module.service.IabCategoriesService;
import com.mcp.rag.llm.module.service.QdrantService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class IabTool {

    private final IabCategoriesService iabService;

    private CacheService cacheService;

    private QdrantService qdrantService;

    @Autowired
    public IabTool(IabCategoriesService iabService, CacheService cacheService, QdrantService qdrantService) {
        this.iabService = iabService;
        this.cacheService = cacheService;
        this.qdrantService = qdrantService;
    }
    
    @Tool(name = "get_all_iabs", description = "List or Get all iab categories in the library")
    public String getAllIabCategories() {
        try {
            List<Iab> iabs = iabService.getAllIabs();
            
            if (iabs.isEmpty()) {
                return "No iabs found in the system";
            }
            
            StringBuilder result = new StringBuilder("Iabs in the library:\n");
            for (Iab iab : iabs) {
                result.append(String.format("ID: %d | '%s' n",
                        iab.getId(), iab.getName()));
            }
            
            result.append(String.format("\nTotal iabs: %d", iabs.size()));
            return result.toString();
        } catch (Exception e) {
            return "Error retrieving iabs: " + e.getMessage();
        }
    }
    
    @Tool(name = "get_iab_by_id", description = "Get a specific iab by its ID")
    public String getIabById(Long iabId) {
        try {
            if (iabId == null || iabId <= 0) {
                return "Error: Iab ID must be a positive number";
            }
            
            Optional<Iab> iabOpt = iabService.getIabById(iabId);
            
            if (iabOpt.isEmpty()) {
                return "Iab with ID " + iabId + " not found";
            }
            
            Iab iab = iabOpt.get();
            return String.format("Iab Details:\nID: %d\nName: '%s'\nTier1: %s\nTier2: %s\nTier3: %s \nTier: %s",
                    iab.getId(), iab.getName(), iab.getTier1(),
                    iab.getTier2(), iab.getTier3(), iab.getTier4());
        } catch (Exception e) {
            return "Error retrieving iab: " + e.getMessage();
        }
    }

    @Tool(name = "search_iabs_by_name_using_qdrant", description = "Search iabs by iab name using qdrant semantic search")
    public String searchIabsByNameUsingQdrant(String iabName) {
        String cacheKey = cacheService.generateCacheKey("SEARCH", iabName);
        List<Iab> iabs = qdrantService.getQdrantSemanticData(iabName);
        StringBuilder strBuilder = new StringBuilder(getSearchResult(iabs));
        strBuilder.append(String.format("\nFound %d iabs", iabs.size()));
        // Store in cache for next time
        cacheService.cacheResult(cacheKey, strBuilder.toString());
        return strBuilder.toString();
    }

    
    @Tool(name = "search_iabs_by_name", description = "Search iabs by iab name (partial match)")
    public String searchIabsByName(String iabName) {
        try {
            if (iabName == null || iabName.trim().isEmpty()) {
                return "Error: Iab name cannot be empty";
            }

            String cacheKey = cacheService.generateCacheKey("SEARCH", iabName);
            String cached = cacheService.getCachedResult(cacheKey, String.class);
            List<Iab> iabs = null;
            if(cached == null || cached.trim().length() == 0){
                // 1. Perform the search directly
                iabs = qdrantService.getQdrantSemanticData(iabName);
                if(iabs == null || iabs.isEmpty()){
                    iabs = iabService.searchByName(iabName.trim());
                    if (iabs.isEmpty()) {
                        return "No iabs found with name containing: " + iabName;
                    }
                }
            }else{
                return cached;
            }
            StringBuilder strBuilder = new StringBuilder(getSearchResult(iabs));
            strBuilder.append(String.format("\nFound %d iabs", iabs.size()));
            // Store in cache for next time
            cacheService.cacheResult(cacheKey, strBuilder.toString());
            return strBuilder.toString();
        } catch (Exception e) {
            return "Error searching iabs: " + e.getMessage();
        }
    }
    @Tool(name="detail_about_developer", description = "This tool will provide some information detail about developer of this repo")
    public String detailAboutDeveloper(){
        return "This developer is a senior backend engineer with expertise in Spring Boot, Kubernetes, Qdrant, Kafka, Docker with Certifications like GCP PCA and CKAD";
    }

    @Tool(name="redis_stats", description = "This tool will provide redis stats used by this mcp server")
    public String getStats(){
        CacheService.CacheStats stats = cacheService.getStats();

        return String.format("""
        Cache Statistics:
        - Enabled: %s
        - Total Cached Entries: %d
        - TTL: %d seconds (%.1f minutes)
        - Status: %s
        """,
                stats.enabled() ? "Yes" : "No",
                stats.totalKeys(),
                stats.ttl(),
                stats.ttl() / 60.0,
                stats.enabled() ? "Active" : "Disabled"
        );
    }

    private String getSearchResult(List<Iab> results){
        return results.stream()
                .map(iab -> String.format("Iab Details:\nID: %d\nName: '%s'\nTier1: %s\nTier2: %s\nTier3: %s \nTier4: %s",
                        iab.getId(), iab.getName(), iab.getTier1(),
                        iab.getTier2(), iab.getTier3(), iab.getTier4()))
                .collect(Collectors.joining("\n"));

    }
}
