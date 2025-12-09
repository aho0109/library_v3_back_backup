package org.matsuzaka.library_v3_back.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "category")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "category_title", nullable = false, unique = true)
    private String categoryTitle;

    // 一個主分類有多個子分類
    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    private List<CategorySub> categorySubs;
}