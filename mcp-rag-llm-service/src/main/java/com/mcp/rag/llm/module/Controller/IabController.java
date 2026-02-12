package com.mcp.rag.llm.module.Controller;

import com.mcp.rag.llm.module.entity.Iab;
import com.mcp.rag.llm.module.service.IabCategoriesService;
import com.mcp.rag.llm.module.service.IabSearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/iab")
public class IabController {

    private final IabCategoriesService iabService;

    private final IabSearchService search;

    public IabController(IabCategoriesService iabService, IabSearchService search) {
        this.iabService = iabService;
        this.search = search;
    }

    // GET /api/iab
    @GetMapping
    public ResponseEntity<List<Iab>> getAll() {
        return ResponseEntity.ok(iabService.getAllIabs());
    }

    // GET /api/iab/123
    @GetMapping("/{id}")
    public ResponseEntity<Iab> getById(@PathVariable Long id) {
        return iabService.getIabById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET /api/iab/search?name=Automotive
    @GetMapping("/search")
    public ResponseEntity<List<Iab>> getByName(@RequestParam String name) {
        List<Iab> results = search.performTripleTierSearch(name);
        return ResponseEntity.ok(results);
    }
}
