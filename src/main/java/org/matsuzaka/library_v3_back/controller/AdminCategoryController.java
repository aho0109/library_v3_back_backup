package org.matsuzaka.library_v3_back.controller;

import org.matsuzaka.library_v3_back.dto.common.ApiResponse;
import org.matsuzaka.library_v3_back.dto.CategoryMainDTO;
import org.matsuzaka.library_v3_back.dto.CategorySubDTO;
import org.matsuzaka.library_v3_back.service.CategoryService;
import org.matsuzaka.library_v3_back.service.CategorySubService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 管理員分類管理控制器
 */
@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminCategoryController {

    private final CategoryService categoryService;
    private final CategorySubService categorySubService;

    public AdminCategoryController(CategoryService categoryService, CategorySubService categorySubService) {
        this.categoryService = categoryService;
        this.categorySubService = categorySubService;
    }

    // ===== 主分類管理 =====

    /**
     * 新增主分類
     */
    @PostMapping("/categories")
    public ResponseEntity<ApiResponse<CategoryMainDTO>> createMainCategory(@RequestBody CategoryMainDTO dto) {
        CategoryMainDTO created = categoryService.createCategory(dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("主分類新增成功", created));
    }

    /**
     * 更新主分類
     */
    @PutMapping("/categories/{id}")
    public ResponseEntity<ApiResponse<CategoryMainDTO>> updateMainCategory(
            @PathVariable Long id, @RequestBody CategoryMainDTO dto) {
        CategoryMainDTO updated = categoryService.updateCategory(id, dto);
        return ResponseEntity.ok(ApiResponse.success("主分類更新成功", updated));
    }

    /**
     * 刪除主分類
     */
    @DeleteMapping("/categories/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMainCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(ApiResponse.success("主分類刪除成功"));
    }

    // ===== 子分類管理 =====

    /**
     * 新增子分類
     */
    @PostMapping("/category-subs")
    public ResponseEntity<ApiResponse<CategorySubDTO>> createSubCategory(@RequestBody CategorySubDTO dto) {
        CategorySubDTO created = categorySubService.createCategorySub(dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("子分類新增成功", created));
    }

    /**
     * 更新子分類
     */
    @PutMapping("/category-subs/{id}")
    public ResponseEntity<ApiResponse<CategorySubDTO>> updateSubCategory(
            @PathVariable Long id, @RequestBody CategorySubDTO dto) {
        CategorySubDTO updated = categorySubService.updateCategorySub(id, dto);
        return ResponseEntity.ok(ApiResponse.success("子分類更新成功", updated));
    }

    /**
     * 刪除子分類
     */
    @DeleteMapping("/category-subs/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSubCategory(@PathVariable Long id) {
        categorySubService.deleteCategorySub(id);
        return ResponseEntity.ok(ApiResponse.success("子分類刪除成功"));
    }
}


