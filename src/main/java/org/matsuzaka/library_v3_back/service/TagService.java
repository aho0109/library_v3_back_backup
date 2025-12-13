package org.matsuzaka.library_v3_back.service;


import org.matsuzaka.library_v3_back.dto.TagDTO;
import org.matsuzaka.library_v3_back.dto.TagTop10DTO;

import java.util.List;

public interface TagService {

    /**
     * 查詢熱門標籤前十名。
     * @return 熱門標籤列表
     */
    List<TagTop10DTO> getTop10(Long categoryId);

    /**
     * 查詢所有標籤。
     * @return 所有標籤列表
     */
    List<TagDTO> getAll();

    // 管理員功能
    List<TagDTO> searchByKeyword(String keyword);
    TagDTO create(TagDTO dto);
    TagDTO update(Long id, TagDTO dto);
    void delete(Long id);
}
