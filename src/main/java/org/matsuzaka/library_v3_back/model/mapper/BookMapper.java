package org.matsuzaka.library_v3_back.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;
import org.matsuzaka.library_v3_back.dto.queryDTO.queryOneDTO.BookRespDtoOneDetails;
import org.matsuzaka.library_v3_back.model.entity.Author;
import org.matsuzaka.library_v3_back.model.entity.Book;
import org.matsuzaka.library_v3_back.model.entity.BookCopy;
import org.matsuzaka.library_v3_back.model.entity.Tag;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {BookCopyMapper.class}) // componentModel = "spring" 讓 Spring 自動管理，uses = {BookCopyMapper.class} 讓它知道如何映射 BookCopy
public interface BookMapper { // 要將 Book 實體映射到 BookRespDtoOneDetails DTO，所以要寫BookMapper

    // 獲取 Mapper 實例
    // Mappers.getMapper(BookMapper.class) 會返回一個 BookMapper 的實例。
    // 這個實例是由 MapStruct 在編譯時自動生成的實作類別（例如 BookMapperImpl）提供的。
    // 這樣你就可以在其他地方使用 BookMapper.INSTANCE 來調用映射方法。
    // 這個 INSTANCE 是一個靜態常量，代表了 BookMapper 的實例。
    // 這樣的設計使得你可以在應用程式的任何地方輕鬆地使用這個 Mapper，而不需要每次都創建新的實例。
    BookMapper INSTANCE = Mappers.getMapper(BookMapper.class);

    // 定義 Book 實體到 BookDetailDto 的映射規則
    //@Mapping(source = "authors.name", target = "authors") // 將 Author 實體的 name 映射到 DTO 的 authors
    @Mapping(target = "authors", expression = "java(mapAuthorsToString(book.getAuthors()))")
    //@Mapping(source = "tags.title", target = "tags") // 將 Tag 實體的 title 映射到 DTO 的 tags
    @Mapping(target = "tags", expression = "java(mapTagsToString(book.getTags()))")
    @Mapping(source = "series.title", target = "seriesTitle") // 將 Series 實體的 title 映射到 DTO 的 seriesTitle
    @Mapping(source = "categorySub.category.categoryTitle", target = "mainCategoryTitle") // 將 Category 實體的 categoryTitle 映射到 DTO 的 mainCategoryTitle，因為多一層巢狀，一定要寫這行
    @Mapping(source = "categorySub.categorySubTitle", target = "subCategoryTitle") // 將 CategorySub 實體的 categorySubTitle 映射到 DTO 的 subCategoryTitle
    @Mapping(source = "publisher.pubName", target = "publisher")     // 將 Publisher 實體的 pubName 映射到 DTO 的 publisher
    @Mapping(target = "availableCopies", expression = "java(mapAvailableCopies(book.getBookCopies()))") // 計算可借閱數量
    @Mapping(target = "totalCopies", expression = "java(mapTotalCopies(book.getBookCopies()))")       // 計算總副本數量
    @Mapping(source = "bookCopies", target = "bookCopies") // MapStruct 會自動使用 BookCopyMapper 映射 List
    // 前者：目標型別 (Target Type)，表示這個方法會返回一個 BookRespDtoOneDetails DTO 的實例。
    // 後者：來源型別 (Source Type)，表示這個方法會接受一個 Book 實體作為參數。
    // toBookRespDtoOneDetails 這是方法的名稱
    // 不需要手動編寫這個方法的方法體！這是 MapStruct 的核心。
    // 當你使用 @Mapper 註解標註 BookMapper 介面時，MapStruct 會在編譯時自動生成一個該介面的實作類別（例如 BookMapperImpl）。
    // 這個生成的實作類別會包含所有必要的程式碼，來執行 Book 實體的屬性到 BookRespDtoOneDetails DTO 屬性的映射，包括你用 @Mapping 註解定義的複雜轉換（例如巢狀屬性、計算屬性、列表轉換等）。
    BookRespDtoOneDetails toBookRespDtoOneDetails(Book book);


    // 基礎上，MapStruct 有隱式映射 (Implicit Mapping)
    // 如果來源物件 (Source Object) 和目標物件 (Target Object) 之間有：
    // 同名的屬性 (Same Property Name) 相容的型別 (Compatible Types)
    // 那麼 MapStruct 會自動地、隱式地將這些屬性進行映射，你不需要為它們撰寫任何 @Mapping 註解。

    // 如果 Book 實體的某些屬性名稱與 DTO 不同，或是以下等特殊狀況，則需要使用 @Mapping 註解來指定來源和目標屬性。

    // 來源是巢狀屬性 (Nested Property)：
    // 你的 Book 實體有一個 Category 物件 (book.getCategory())。你想要映射的是 Category 物件裡面的 categoryTitle 屬性。MapStruct 不會自動深入巢狀結構。
    // 來源和目標屬性名稱不匹配： 來源是 categoryTitle，但目標 DTO 的欄位是 category。名稱不同，所以需要明確指定。

    // 來源不是直接的屬性，而是計算結果 (Derived/Calculated Properties)：
    // 你的 Book 實體中並沒有 availableCopies 或 totalCopies 這兩個直接的屬性。這些值是需要透過遍歷 bookCopies 集合並進行聚合計算得出的。
    // 需要自定義邏輯： MapStruct 無法自動「理解」如何從一個集合中計算出這些聚合值。
    // 因此，你需要使用 expression = "java(...)" 來告訴 MapStruct 如何計算這些值。

    // 來源是 集合中包含需要進一步映射的複雜物件 (需要 另一個 Mapper)
    // List<BookCopy>，當中還需要再使用 BookCopyMapper 來映射每個 BookCopy 實體到 BookCopyRespDto。
    // 這裡的 bookCopies 是 Book 實體中的屬性，MapStruct 會自動使用 BookCopyMapper 來映射每個 BookCopy 實體到 BookCopyRespDto。
    // 雖然來源 (book.getBookCopies()) 和目標 (BookDetailDto.bookCopies) 的屬性名稱都是 bookCopies 且型別都是 List，但它們的元素型別不同 (BookCopy 實體 vs. BookCopyDto DTO)。
    // MapStruct 需要知道如何將 List<BookCopy> 中的每個 BookCopy 物件轉換為 BookCopyDto。


    // 將作者集合轉換為字串集合
    @Named("mapAuthorsToString")
    default Set<String> mapAuthorsToString(Set<Author> authors) {
        if (authors == null || authors.isEmpty()) {
            return new HashSet<>();
        }
        return authors.stream()
                .map(Author::getName)
                // 這是 Java 8加入了lambda表示式(lambda expression)的語法特性，而lambda表示式可以進一步改用 method references（方法參考/方法引用）的寫法
                // Method references即使可以進一步簡化Lambda語法，但仍有使用上的限制。只有當lambda中僅執行一個方法的情況下才能改以method references語法來撰寫。。
                // 對 Stream 中的每個 Author 物件，呼叫其 getName() 方法，取得作者名字，並將原本的 Author 物件轉換成 String 型別（作者名字）。
                // map 方法會根據你提供的函式回傳值，將原本的元素型別轉換成對應的新型別。
                .collect(Collectors.toSet());
                // 還有以下常見的 Stream 終端收集方法：
                // .collect(Collectors.toList())                      收集成 List，例如：List<String>
                // .collect(Collectors.toMap(keyMapper, valueMapper)) 收集成 Map，可以自訂 key 和 value 的映射方式
                // .collect(Collectors.joining(","))                  將所有元素合併成一個字串，中間用逗號分隔
                // .collect(Collectors.toCollection(TreeSet::new))    收集成指定型別的集合，例如 TreeSet
                // .collect(Collectors.groupingBy(...))               根據某個屬性分組，收集成 Map

    }

    // 將標籤集合轉換為字串集合
    @Named("mapTagsToString")
    default Set<String> mapTagsToString(Set<Tag> tags) {
        if (tags == null || tags.isEmpty()) {
            return new HashSet<>();
        }
        return tags.stream()
                .map(Tag::getTitle)
                .collect(Collectors.toSet());
    }



    // --- 輔助方法 (由 MapStruct 調用) ---
    // 這些方法不需要手動實現，MapStruct 會在生成程式碼時調用它們來計算值
    @Named("mapAvailableCopies")
    default BigDecimal mapAvailableCopies(Set<BookCopy> bookCopies) {
        if (bookCopies == null) {
            return BigDecimal.ZERO;
        }
        long count = bookCopies.stream()
                .filter(copy -> "A".equals(copy.getStatus().name()))
                .count();
        return BigDecimal.valueOf(count);
    }
    @Named("mapTotalCopies")
    default Long mapTotalCopies(Set<BookCopy> bookCopies) {
        if (bookCopies == null) {
            return 0L;
        }
        return (long) bookCopies.size();
    }

    // 將狀態 ENUM 轉換為中文描述 (可選，也可以放在 BookCopyMapper 或 DTO 內部)
    @Named("mapStatusToChinese")
    default String mapStatusToChinese(String statusCode) {
        switch (statusCode) {
            case "A": return "可借閱";
            case "L": return "已借出";
            case "R": return "已預約";
            default: return "未知狀態";
        }
    }
}
