package org.matsuzaka.library_v3_back.service;


import org.matsuzaka.library_v3_back.dto.CategorySubDTO;

import java.util.List;

public interface CategorySubService {

    // 列出所有 CategorySub
    List<CategorySubDTO> getAllCategorySubs();

    // 管理員功能
    CategorySubDTO createCategorySub(CategorySubDTO dto);
    CategorySubDTO updateCategorySub(Long id, CategorySubDTO dto);
    void deleteCategorySub(Long id);

    }
