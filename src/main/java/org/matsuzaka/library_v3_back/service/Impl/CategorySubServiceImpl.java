package org.matsuzaka.library_v3_back.service.Impl;

import org.matsuzaka.library_v3_back.dto.CategorySubDTO;
import org.matsuzaka.library_v3_back.model.entity.Category;
import org.matsuzaka.library_v3_back.model.entity.CategorySub;
import org.matsuzaka.library_v3_back.model.repositoryDao.CategoryRepository;
import org.matsuzaka.library_v3_back.model.repositoryDao.CategorySubRepository;
import org.matsuzaka.library_v3_back.service.CategorySubService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategorySubServiceImpl implements CategorySubService {

    private CategorySubRepository categorySubRepository;
    private CategoryRepository categoryRepository;
    
    public CategorySubServiceImpl(CategorySubRepository categorySubRepository, CategoryRepository categoryRepository) {
        this.categorySubRepository = categorySubRepository;
        this.categoryRepository = categoryRepository;
    }

    // 列出所有 CategorySub
    public List<CategorySubDTO> getAllCategorySubs() {
    List<CategorySubDTO> categorySubDTOS = categorySubRepository.findAllByOrderByIdAsc()
            .stream()
            .map(categorySub -> new CategorySubDTO(
                    categorySub.getId(),
                    categorySub.getCategorySubTitle()))
            .toList();
        return  categorySubDTOS;

    }

    @Override
    public CategorySubDTO createCategorySub(CategorySubDTO dto) {
        // 檢查主分類是否存在
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("主分類不存在"));
        
        CategorySub sub = new CategorySub();
        sub.setCategorySubTitle(dto.getCategorySubTitle());
        sub.setCategory(category);
        CategorySub saved = categorySubRepository.save(sub);
        
        CategorySubDTO result = new CategorySubDTO();
        result.setId(saved.getId());
        result.setCategorySubTitle(saved.getCategorySubTitle());
        result.setCategoryId(saved.getCategory().getId());
        return result;
    }

    @Override
    public CategorySubDTO updateCategorySub(Long id, CategorySubDTO dto) {
        CategorySub sub = categorySubRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("子分類不存在"));
        
        sub.setCategorySubTitle(dto.getCategorySubTitle());
        
        // 如果要更新主分類
        if (dto.getCategoryId() != null && !dto.getCategoryId().equals(sub.getCategory().getId())) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("主分類不存在"));
            sub.setCategory(category);
        }
        
        CategorySub updated = categorySubRepository.save(sub);
        
        CategorySubDTO result = new CategorySubDTO();
        result.setId(updated.getId());
        result.setCategorySubTitle(updated.getCategorySubTitle());
        result.setCategoryId(updated.getCategory().getId());
        return result;
    }

    @Override
    public void deleteCategorySub(Long id) {
        CategorySub sub = categorySubRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("子分類不存在"));
        
        // 檢查是否有書籍使用此子分類
        if (!sub.getBooks().isEmpty()) {
            throw new RuntimeException("無法刪除有書籍的子分類");
        }
        
        categorySubRepository.delete(sub);
    }

}
