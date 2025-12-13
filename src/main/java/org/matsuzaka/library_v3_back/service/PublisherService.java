package org.matsuzaka.library_v3_back.service;


import org.matsuzaka.library_v3_back.dto.PublisherDTO;

import java.util.List;

public interface PublisherService {

    List<PublisherDTO> getAll();

    // 管理員功能
    List<PublisherDTO> searchByKeyword(String keyword);
    PublisherDTO create(PublisherDTO dto);
    PublisherDTO update(Long id, PublisherDTO dto);
    void delete(Long id);

}
