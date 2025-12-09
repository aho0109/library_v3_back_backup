package org.matsuzaka.library_v3_back.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TagTop10DTO {

    private Long id; // 標籤的唯一識別ID

    private String title; // 標籤名稱

}
