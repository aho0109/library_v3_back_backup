package org.matsuzaka.library_v3_back.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryMainDTO {

    private Long id;

    private String categoryTitle;

    private List<CategorySubDTO> categorySubs;

}
