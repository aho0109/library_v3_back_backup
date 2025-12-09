package org.matsuzaka.library_v3_back.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "book")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"authors", "tags", "bookCopies", "series", "categorySub", "publisher"}) // 關鍵修正: 排除所有關聯
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

    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "ISBN", nullable = false, unique = true, length = 20)
    private String isbn;

    // 方便顯示系列代表作
    @Column(name = "representative", nullable = false)
    private Boolean representative = false;
    //private Integer representative; // 0: 非代表作, 1: 代表作
    // JPA 會自動理 Boolean 到 TINYINT 的映射，將 true/false 轉換成 1/0
    // 若要更明確控制，可以寫(name = "representative", nullable = false, columnDefinition = "TINYINT(1)")

    // 書籍與作者是多對多關係
    // 多對多是平等關係，但框架規定還是要分主導方，因此可依專案目的決定，目前因書為主體牽動一切故選書
    // CascadeType一般都在主導方，非必寫，寫了之後在連動新增連動修改時會少寫一些業務邏輯，沒寫就常規需自己編寫管控邏輯
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "book_author", // 中間表名稱
            joinColumns = @JoinColumn(name = "book_id"), // 本實體 (Book) 在中間表的 ID
            inverseJoinColumns = @JoinColumn(name = "author_id") // 關聯實體 (Author) 在中間表的 ID
    )
    //@EqualsAndHashCode.Exclude // 關鍵修正：排除集合關聯
    @BatchSize(size = 10)
    // 批量載入，減少查詢次數
    // @BatchSize(size = 10) 的優點是能減少 N+1 查詢問題，提升批量載入效率。
    // 但它只是建議 Hibernate 每次批量抓取的數量，並不會自動根據 controller 的需求動態調整。
    // 如果 controller 一次要 20 筆，這裡設 10，Hibernate 會分兩批查詢。controller 決定一次查詢多少主資料（如 20 本書），而 @BatchSize(size = 10) 決定每次載入關聯（如作者、標籤）時的批量數量。
    private Set<Author> authors = new HashSet<>();

    // 書籍與標籤是多對多關係
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "book_tag",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    //@JsonIgnoreProperties("books") // 告訴 Jackson 忽略 Tag 裡的 books 欄位，兩方都要寫。（原本的JsonManagedReference/JsonBackReference不夠處理）但最佳實踐來說全面只用dto，entity不該跟序列化（前端互動）有關聯
    //@EqualsAndHashCode.Exclude // 關鍵修正：排除集合關聯
    @BatchSize(size = 10) // 批量載入，減少查詢次數
    private Set<Tag> tags = new HashSet<>();

    // 一本書籍有多個實體副本
    // 多方(BookCopy)通常是擁有方，所以這裡一方(Book)是被擁有方
    @OneToMany(mappedBy = "book", fetch = FetchType.LAZY)
    //@EqualsAndHashCode.Exclude // 關鍵修正：排除集合關聯
    private Set<BookCopy> bookCopies;





    // Helper methods for ManyToMany relationships (optional but good practice)
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