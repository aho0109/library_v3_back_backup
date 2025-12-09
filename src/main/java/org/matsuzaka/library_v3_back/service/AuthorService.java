package org.matsuzaka.library_v3_back.service;


import org.matsuzaka.library_v3_back.dto.AuthorDTO;

import java.util.List;

public interface AuthorService {

    /**
     * 查詢所有作者。
     * @return 所有標籤列表
     */
    List<AuthorDTO> getAll();

}
