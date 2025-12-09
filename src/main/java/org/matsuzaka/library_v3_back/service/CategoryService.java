package org.matsuzaka.library_v3_back.service;

import org.matsuzaka.library_v3_back.dto.CategoryMainDTO;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;

public interface CategoryService {

    @EntityGraph(attributePaths = {"categorySubs"})
    List<CategoryMainDTO> getAll();

    CategoryMainDTO getById(Long categoryId);

}
