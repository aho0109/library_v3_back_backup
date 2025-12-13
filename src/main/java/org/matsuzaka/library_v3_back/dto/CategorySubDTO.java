package org.matsuzaka.library_v3_back.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategorySubDTO {

    private Long id;

    private String categorySubTitle;
    
    private Long categoryId; // 所屬主分類ID

    public CategorySubDTO(Long id, String categorySubTitle) {
    }
}
