package com.mcp.rag.llm.module.service;

import com.mcp.rag.llm.module.entity.Iab;
import com.mcp.rag.llm.module.repository.IabRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class IabCategoriesService {

    private final IabRepository iabRepository;

    @Autowired
    public IabCategoriesService(IabRepository iabRepository) {
        this.iabRepository = iabRepository;
    }



    /**
     * Get all Iab
     */
    @Transactional(readOnly = true)
    public List<Iab> getAllIabs() {
        return iabRepository.findAll();
    }

    /**
     * Get Iab by ID
     */
    @Transactional(readOnly = true)
    public Optional<Iab> getIabById(Long id) {
        return iabRepository.findById(id);
    }

    /**
     * Search Iab by name
     */
    @Transactional(readOnly = true)
    public List<Iab> searchByName(String name) {
        return iabRepository.findByNameContainingIgnoreCase(name);
    }

    /**
     * Add a new iab
     */
    public void addIab(@Valid Iab iab) {
        iabRepository.insertWithId(iab.getId(), iab.getParentId(), iab.getName(), iab.getTier1(), iab.getTier2(), iab.getTier3(), iab.getTier4());
    }
}

