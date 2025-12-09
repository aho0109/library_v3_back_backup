package org.matsuzaka.library_v3_back.service;


import org.matsuzaka.library_v3_back.dto.PublisherDTO;

import java.util.List;

public interface PublisherService {

    List<PublisherDTO> getAll();

}
