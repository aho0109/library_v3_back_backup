package org.matsuzaka.library_v3_back.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "category_sub")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategorySub {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 多個子分類屬於一個主分類
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "category_sub_title", nullable = false)
    private String categorySubTitle;

    // 一個子分類有多本書籍
    @OneToMany(mappedBy = "categorySub", fetch = FetchType.LAZY)
    private List<Book> books;
}