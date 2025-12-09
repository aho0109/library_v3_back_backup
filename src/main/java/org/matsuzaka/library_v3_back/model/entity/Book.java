package org.matsuzaka.library_v3_back.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "book")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"authors", "tags", "bookCopies", "reviews", "series", "categorySub", "publisher"}) // 關鍵修正: 排除所有關聯
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    // 多本書籍屬於一個系列 (可為 NULL)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "series_id") // series_id 欄位
    //@EqualsAndHashCode.Exclude // 排除單值關聯
    private Series series;

    // 多本書籍屬於一個子分類
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_sub_id", nullable = false)
    //@EqualsAndHashCode.Exclude // 排除單值關聯
    private CategorySub categorySub;

    @Column(name = "publish_year")
    private Short publishYear;

    // 多本書籍屬於一個出版社
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "publisher_id", nullable = false)
    //@EqualsAndHashCode.Exclude // 排除單值關聯
    private Publisher publisher;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "ISBN", nullable = false, unique = true, length = 20)
    private String isbn;

    // 方便顯示系列代表作
    @Column(name = "representative", nullable = false)
    private Boolean representative = false;

    @Column(name = "average_rating", precision = 2, scale = 1)
    private BigDecimal averageRating;

    @Column(name = "rating_count", nullable = false)
    private Integer ratingCount = 0;

    @Column(name = "added_date", nullable = false)
    private LocalDate addedDate;

    @Column(name = "total_loan_count", nullable = false)
    private Integer totalLoanCount = 0;

    // 書籍與作者是多對多關係
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "book_author", // 中間表名稱
            joinColumns = @JoinColumn(name = "book_id"), // 本實體 (Book) 在中間表的 ID
            inverseJoinColumns = @JoinColumn(name = "author_id") // 關聯實體 (Author) 在中間表的 ID
    )
    @BatchSize(size = 10)
    private Set<Author> authors = new HashSet<>();

    // 書籍與標籤是多對多關係
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "book_tag",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @BatchSize(size = 10) // 批量載入，減少查詢次數
    private Set<Tag> tags = new HashSet<>();

    // 一本書籍有多個實體副本
    @OneToMany(mappedBy = "book", fetch = FetchType.LAZY)
    private Set<BookCopy> bookCopies;

    // 一本書籍有多個評論
    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Review> reviews;

    // Helper methods
    public void addAuthor(Author author) {
        this.authors.add(author);
        author.getBooks().add(this);
    }

    public void removeAuthor(Author author) {
        this.authors.remove(author);
        author.getBooks().remove(this);
    }

    public void addTag(Tag tag) {
        this.tags.add(tag);
        tag.getBooks().add(this);
    }

    public void removeTag(Tag tag) {
        this.tags.remove(tag);
        tag.getBooks().remove(this);
    }
}

