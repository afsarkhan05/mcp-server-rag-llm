package com.mcp.rag.llm.module.tool;

import com.mcp.rag.llm.module.entity.Iab;
import com.mcp.rag.llm.module.service.IabCategoriesService;
import com.mcp.rag.llm.module.service.IabSearchService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class IabTool {

    private final IabCategoriesService iabService;
    private final IabSearchService service;

    public IabTool(IabCategoriesService iabService, IabSearchService service) {
        this.iabService = iabService;
        this.service = service;
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

    @Tool(name = "search_iabs_by_name", description = "Search IAB categories semantically or by name.")
    public String searchIabsByName(@ToolParam(description = "The search query or category name") String query) {

        // 1. Perform the search directly
        List<Iab> results = service.performTripleTierSearch(query);

        // 2. Return a direct String or List for the LLM
        if (results.isEmpty()) {
            return "No IAB categories found.";
        }

        return results.stream()
                .map(i -> String.format("[%d] %s (Path: %s > %s)",
                        i.getId(), i.getName(), i.getTier1(), i.getTier2()))
                .collect(Collectors.joining("\n"));
    }

    
    /*@Tool(name = "search_iabs_by_name",
            description = "Search iabs by name (partial match). Results are cached for faster subsequent queries.")
    public String searchIabsByName(String iabName) {
        try {
            if (iabName == null || iabName.trim().isEmpty()) {
                return "Error: Iab name cannot be empty";
            }

            // Generate cache key
            String cacheKey = cacheService.generateCacheKey(CACHE_PREFIX_SEARCH, iabName);

            // Try to get from cache
            String cachedResult = cacheService.getCachedResult(cacheKey, String.class);
            if (cachedResult != null) {
                return "[FROM CACHE] " + cachedResult;
            }
            
            List<Iab> iabs = iabService.searchByName(iabName.trim());
            
            if (iabs.isEmpty()) {
                return "No iabs found with name containing: " + iabName;
            }
            
            StringBuilder result = new StringBuilder(String.format("Iabs matching '%s':\n", iabName));
            for (Iab iab : iabs) {
                result.append(String.format("Iab Details:\nID: %d\nName: '%s'\nTier1: %s\nTier2: %s\nTier3: %s \nTier: %s",
                        iab.getId(), iab.getName(), iab.getTier1(),
                        iab.getTier2(), iab.getTier3(), iab.getTier4()));
            }
            
            result.append(String.format("\nFound %d iabs", iabs.size()));
            // Cache the result
            cacheService.cacheResult(cacheKey, result);
            return result.toString();
        } catch (Exception e) {
            return "Error searching iabs: " + e.getMessage();
        }
    }*/

/*    @Tool(name = "get_cache_stats",
            description = "Get Redis cache statistics")
    public String getCacheStats() {
        return service.getStats();
    }*/
}
