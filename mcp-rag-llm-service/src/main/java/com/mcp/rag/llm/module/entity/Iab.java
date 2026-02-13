package com.mcp.rag.llm.module.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "iab")
@Data
public class Iab {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "parent_id", nullable = true)
    private Long parentId;

    @NotBlank(message = "IAB name is required")
    @Column(name = "name", nullable = false)
    private String name;

    @NotBlank(message = "tier1 is required")
    @Column(name = "tier1", nullable = false)
    private String tier1;

    @Column(name = "tier2", nullable = true)
    private String tier2;

    @Column(name = "tier3", nullable = true)
    private String tier3;

    @Column(name = "tier4", nullable = true)
    private String tier4;

    // Default constructor
    public Iab() {}

    // Constructor with parameters
    public Iab(Long id, Long parentId, String name, String tier1, String tier2, String tier3, String tier4) {
        this.id = id;
        this.name = name;
        this.tier1 = tier1;
        this.tier2 = tier2;
        this.tier3 = tier3;
        this.tier4 = tier4;
        this.parentId = parentId;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "IAB{" +
                "id=" + id +
                ", parentId='" + parentId + '\'' +
                ", name='" + name + '\'' +
                ", tier1='" + tier1 + '\'' +
                ", tier2='" + tier2 + '\'' +
                ", tier3='" + tier3 + '\'' +
                ", tier4='" + tier4 + '\'' +
                '}';
    }
}
