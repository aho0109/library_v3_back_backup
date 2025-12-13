package org.matsuzaka.library_v3_back.service;


import org.matsuzaka.library_v3_back.dto.AuthorDTO;

import java.util.List;

public interface AuthorService {

    /**
     * 查詢所有作者。
     * @return 所有標籤列表
     */
    List<AuthorDTO> getAll();

    // 管理員功能
    List<AuthorDTO> searchByKeyword(String keyword);
    AuthorDTO create(AuthorDTO dto);
    AuthorDTO update(Long id, AuthorDTO dto);
    void delete(Long id);

}
