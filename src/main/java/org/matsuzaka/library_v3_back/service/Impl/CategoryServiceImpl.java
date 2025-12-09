package org.matsuzaka.library_v3_back.service.Impl;

import org.matsuzaka.library_v3_back.dto.CategoryMainDTO;
import org.matsuzaka.library_v3_back.dto.CategorySubDTO;
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
}
