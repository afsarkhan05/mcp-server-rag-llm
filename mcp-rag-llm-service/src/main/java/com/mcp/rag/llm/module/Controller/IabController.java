package com.mcp.rag.llm.module.Controller;

import com.mcp.rag.llm.module.entity.Iab;
import com.mcp.rag.llm.module.service.IabCategoriesService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/iab")
public class IabController {

    private final IabCategoriesService iabService;

    public IabController(IabCategoriesService iabService) {
        this.iabService = iabService;
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
        List<Iab> results = iabService.searchByName(name);
        return ResponseEntity.ok(results);
    }
}
