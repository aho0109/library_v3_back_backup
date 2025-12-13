package org.matsuzaka.library_v3_back.controller;

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
    public ResponseEntity<?> createMainCategory(@RequestBody CategoryMainDTO dto) {
        try {
            CategoryMainDTO created = categoryService.createCategory(dto);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 更新主分類
     */
    @PutMapping("/categories/{id}")
    public ResponseEntity<?> updateMainCategory(@PathVariable Long id, @RequestBody CategoryMainDTO dto) {
        try {
            CategoryMainDTO updated = categoryService.updateCategory(id, dto);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 刪除主分類
     */
    @DeleteMapping("/categories/{id}")
    public ResponseEntity<String> deleteMainCategory(@PathVariable Long id) {
        try {
            categoryService.deleteCategory(id);
            return ResponseEntity.ok("主分類刪除成功");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ===== 子分類管理 =====

    /**
     * 新增子分類
     */
    @PostMapping("/category-subs")
    public ResponseEntity<?> createSubCategory(@RequestBody CategorySubDTO dto) {
        try {
            CategorySubDTO created = categorySubService.createCategorySub(dto);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 更新子分類
     */
    @PutMapping("/category-subs/{id}")
    public ResponseEntity<?> updateSubCategory(@PathVariable Long id, @RequestBody CategorySubDTO dto) {
        try {
            CategorySubDTO updated = categorySubService.updateCategorySub(id, dto);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 刪除子分類
     */
    @DeleteMapping("/category-subs/{id}")
    public ResponseEntity<String> deleteSubCategory(@PathVariable Long id) {
        try {
            categorySubService.deleteCategorySub(id);
            return ResponseEntity.ok("子分類刪除成功");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}

