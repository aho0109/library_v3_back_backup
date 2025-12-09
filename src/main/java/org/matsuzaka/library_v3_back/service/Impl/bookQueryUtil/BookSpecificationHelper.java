package org.matsuzaka.library_v3_back.service.Impl.bookQueryUtil;

import jakarta.persistence.criteria.*;
import org.matsuzaka.library_v3_back.model.entity.Book;
import org.matsuzaka.library_v3_back.model.entity.Category;
import org.matsuzaka.library_v3_back.model.entity.CategorySub;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class to build Specifications and apply filter conditions for Book queries.
 */
public class BookSpecificationHelper {
    public static Specification<Book> buildFilterSpec(String keyword, Long mainCategoryId, Long subCategoryId,
                                                      Integer seriesDisplay, Long authorId, Long publisherId,
                                                      List<Long> tagIds, Long seriesId,
                                                      String authorKeyword, String publisherKeyword, String bookTitleKeyword,
                                                      Short publishYear, String isbn) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            Map<String, Join<?, ?>> joinMap = new HashMap<>();
            applyFilterConditions(root, query, cb, predicates,
                    keyword, mainCategoryId, subCategoryId, seriesDisplay,
                    authorId, publisherId, tagIds, seriesId,
                    authorKeyword, publisherKeyword, bookTitleKeyword, publishYear, isbn, joinMap);
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    @SuppressWarnings("unchecked")
    public static <X, Y> Join<X, Y> getOrCreateJoin(From<?, X> from,
                                                    String attribute,
                                                    Map<String, Join<?, ?>> joinMap) {
        String key = from.getJavaType().getSimpleName() + "." + attribute;
        if (joinMap.containsKey(key)) {
            return (Join<X, Y>) joinMap.get(key);
        } else {
            Join<X, Y> join = from.join(attribute, JoinType.LEFT);
            joinMap.put(key, join);
            return join;
        }
    }

    public static void applyFilterConditions(Root<Book> root,
                                             CriteriaQuery<?> query,
                                             CriteriaBuilder cb,
                                             List<Predicate> predicates,
                                             String keyword,
                                             Long mainCategoryId,
                                             Long subCategoryId,
                                             Integer seriesDisplay,
                                             Long authorId,
                                             Long publisherId,
                                             List<Long> tagIds,
                                             Long seriesId,
                                             String authorKeyword,
                                             String publisherKeyword,
                                             String bookTitleKeyword,
                                             Short publishYear,
                                             String isbn,
                                             Map<String, Join<?, ?>> joinMap) {
        // Series display filter
        if (seriesDisplay != null && seriesDisplay == 1) {
            predicates.add(cb.isTrue(root.get("representative")));
        }

        // Keyword search
        if (keyword != null && !keyword.isEmpty()) {
            String likeKeyword = "%" + keyword.toLowerCase() + "%";
            predicates.add(cb.or(
                    cb.like(cb.lower(root.get("title")), likeKeyword),
                    cb.like(cb.lower(getOrCreateJoin(root, "authors", joinMap).get("name")), likeKeyword)
            ));
        }

        // Main category
        if (mainCategoryId != null) {
            Join<Book, CategorySub> subJoin = getOrCreateJoin(root, "categorySub", joinMap);
            Join<CategorySub, Category> catJoin = getOrCreateJoin(subJoin, "category", joinMap);
            predicates.add(cb.equal(catJoin.get("id"), mainCategoryId));
        }

        // Sub category
        if (subCategoryId != null) {
            Join<Book, CategorySub> subJoin = getOrCreateJoin(root, "categorySub", joinMap);
            predicates.add(cb.equal(subJoin.get("id"), subCategoryId));
        }

        // Author filter
        if (authorId != null) {
            predicates.add(cb.equal(getOrCreateJoin(root, "authors", joinMap).get("id"), authorId));
        }

        // Publisher filter
        if (publisherId != null) {
            predicates.add(cb.equal(getOrCreateJoin(root, "publisher", joinMap).get("id"), publisherId));
        }

        // Tag filter
        if (tagIds != null && !tagIds.isEmpty()) {
            predicates.add(getOrCreateJoin(root, "tags", joinMap).get("id").in(tagIds));
        }

        // Series filter
        if (seriesId != null) {
            predicates.add(getOrCreateJoin(root, "series", joinMap).get("id").in(seriesId));
        }

        // Author keyword search
        if (authorKeyword != null && !authorKeyword.isEmpty()) {
            String likeAuthorKeyword = "%" + authorKeyword.toLowerCase() + "%";
            predicates.add(cb.like(cb.lower(getOrCreateJoin(root, "authors", joinMap).get("name")), likeAuthorKeyword));
        }

        // Publisher keyword search
        if (publisherKeyword != null && !publisherKeyword.isEmpty()) {
            String likePublisherKeyword = "%" + publisherKeyword.toLowerCase() + "%";
            predicates.add(cb.like(cb.lower(getOrCreateJoin(root, "publisher", joinMap).get("pubName")), likePublisherKeyword));
        }

        // Book title keyword search
        if (bookTitleKeyword != null && !bookTitleKeyword.isEmpty()) {
            String likeBookTitleKeyword = "%" + bookTitleKeyword.toLowerCase() + "%";
            predicates.add(cb.like(cb.lower(root.get("title")), likeBookTitleKeyword));
        }

        // Publish year filter
        if (publishYear != null) {
            predicates.add(cb.equal(root.get("publishYear"), publishYear));
        }

        // ISBN filter
        if (isbn != null && !isbn.isEmpty()) {
            predicates.add(cb.like(root.get("isbn"), "%" + isbn + "%"));
        }
    }
}
