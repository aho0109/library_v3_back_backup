package org.matsuzaka.library_v3_back.service.Impl;

import org.matsuzaka.library_v3_back.dto.CategoryMainDTO;
import org.matsuzaka.library_v3_back.dto.CategorySubDTO;
import org.matsuzaka.library_v3_back.exception.BusinessException;
import org.matsuzaka.library_v3_back.exception.ErrorCode;
import org.matsuzaka.library_v3_back.exception.ResourceNotFoundException;
import org.matsuzaka.library_v3_back.model.entity.Category;
import org.matsuzaka.library_v3_back.model.repositoryDao.CategoryRepository;
import org.matsuzaka.library_v3_back.service.CategoryService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }


    @Override
    public List<CategoryMainDTO> getAll() {
        List<Category> categories = categoryRepository.findAll();
        List<CategoryMainDTO> categoryMainDTOs = categories.stream().map(category -> {
            CategoryMainDTO dto = new CategoryMainDTO();
            dto.setId(category.getId());
            dto.setCategoryTitle(category.getCategoryTitle());
            dto.setCategorySubs(category.getCategorySubs().stream().map(subCategory -> {
                CategorySubDTO subDto = new CategorySubDTO();
                subDto.setId(subCategory.getId());
                subDto.setCategorySubTitle(subCategory.getCategorySubTitle());
                return subDto;
            }).toList());
            return dto;
        }).toList();
        return categoryMainDTOs;
    }

    @Override
    public CategoryMainDTO getById(Long categoryId) {
        return null;
    }

    @Override
    public CategoryMainDTO createCategory(CategoryMainDTO dto) {
        Category category = new Category();
        category.setCategoryTitle(dto.getCategoryTitle());
        Category saved = categoryRepository.save(category);
        
        CategoryMainDTO result = new CategoryMainDTO();
        result.setId(saved.getId());
        result.setCategoryTitle(saved.getCategoryTitle());
        return result;
    }

    @Override
    public CategoryMainDTO updateCategory(Long id, CategoryMainDTO dto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CATEGORY_NOT_FOUND, "分類ID: " + id));

        category.setCategoryTitle(dto.getCategoryTitle());
        Category updated = categoryRepository.save(category);
        
        CategoryMainDTO result = new CategoryMainDTO();
        result.setId(updated.getId());
        result.setCategoryTitle(updated.getCategoryTitle());
        return result;
    }

    @Override
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CATEGORY_NOT_FOUND, "分類ID: " + id));

        // 檢查是否有子分類
        if (!category.getCategorySubs().isEmpty()) {
            throw new BusinessException(ErrorCode.CATEGORY_HAS_SUBCATEGORIES);
        }
        
        categoryRepository.delete(category);
    }
}
