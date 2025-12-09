package org.matsuzaka.library_v3_back.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "author")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Author {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    // 作者與書籍是多對多關係
    // mappedBy 屬性必須放在被擁有方 (Inverse Side / Non-Owning Side)
    // 因為圖書館操作流程來說，以書為主導（通常是先有書，再指定作者），所以主導權在 Book 實體上。
    @ManyToMany(mappedBy = "authors", fetch = FetchType.LAZY)
    private Set<Book> books = new HashSet<>(); // 使用 Set 避免重複
}

