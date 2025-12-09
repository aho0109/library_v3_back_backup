package org.matsuzaka.library_v3_back.service.Impl;

import org.matsuzaka.library_v3_back.dto.CategorySubDTO;
import org.matsuzaka.library_v3_back.model.repositoryDao.CategorySubRepository;
import org.matsuzaka.library_v3_back.service.CategorySubService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategorySubServiceImpl implements CategorySubService {

    private CategorySubRepository categorySubRepository;
    public CategorySubServiceImpl(CategorySubRepository categorySubRepository) {
        this.categorySubRepository = categorySubRepository;
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

}
