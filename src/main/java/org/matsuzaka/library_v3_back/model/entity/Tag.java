package org.matsuzaka.library_v3_back.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tag")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false, unique = true, length = 100)
    private String title;

    // 標籤與書籍是多對多關係
    @ManyToMany(mappedBy = "tags", fetch = FetchType.LAZY)
    //@JsonIgnoreProperties("tags") // 告訴 Jackson 忽略 Book 裡的 tags 欄位
    private Set<Book> books = new HashSet<>();
}
