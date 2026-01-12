package org.matsuzaka.library_v3_back.controller;

import org.matsuzaka.library_v3_back.dto.common.ApiResponse;
import org.matsuzaka.library_v3_back.dto.CategorySubDTO;
import org.matsuzaka.library_v3_back.service.CategorySubService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashSet;
import java.util.Set;

@RestController
@RequestMapping("/api/categorySubs")
@CrossOrigin(origins = "*")
public class CategorySubController {

    private final CategorySubService categorySubService;

    public CategorySubController(CategorySubService categorySubService) {
        this.categorySubService = categorySubService;
    }

    /**
     * 列出所有子分類
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Set<CategorySubDTO>>> getAllCategorySubs() {
        Set<CategorySubDTO> result = new LinkedHashSet<>(categorySubService.getAllCategorySubs());
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
