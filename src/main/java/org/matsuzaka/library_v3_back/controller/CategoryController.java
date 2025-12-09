package org.matsuzaka.library_v3_back.controller;

import org.matsuzaka.library_v3_back.dto.CategoryMainDTO;
import org.matsuzaka.library_v3_back.service.CategoryService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "*")
public class CategoryController {

    private final CategoryService categoryService;
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public List<CategoryMainDTO> getAll() {
        return categoryService.getAll();
    }

}
