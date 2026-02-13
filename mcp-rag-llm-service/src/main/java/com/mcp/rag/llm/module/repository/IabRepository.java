package com.mcp.rag.llm.module.repository;

import com.mcp.rag.llm.module.entity.Iab;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface IabRepository extends JpaRepository<Iab, Long> {
    
    /**
     * Find iabs by book name (case-insensitive)
     */
    List<Iab> findByNameContainingIgnoreCase(String name);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO iab (id, parent_id, name, tier1, tier2, tier3, tier4) VALUES (:id, :parent_id, :name, :tier1, :tier2, :tier3, :tier4)", nativeQuery = true)
    void insertWithId(@Param("id") Long id, @Param("parent_id") Long parentId, @Param("name") String name
            , @Param("tier1") String tier1, @Param("tier2") String tier2, @Param("tier3") String tier3, @Param("tier4") String tier4);


}
