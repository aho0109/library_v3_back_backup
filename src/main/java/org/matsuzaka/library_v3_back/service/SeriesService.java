package org.matsuzaka.library_v3_back.service;


import org.matsuzaka.library_v3_back.dto.SeriesDTO;

import java.util.List;

public interface SeriesService {

    /**
     * 獲取所有系列的列表
     * @return 包含所有系列的 DTO 列表
     */
    List<SeriesDTO> getAll();

}
