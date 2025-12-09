package org.matsuzaka.library_v3_back.service.Impl;

import org.matsuzaka.library_v3_back.dto.PublisherDTO;
import org.matsuzaka.library_v3_back.model.repositoryDao.PublisherRepository;
import org.matsuzaka.library_v3_back.service.PublisherService;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class PublisherServiceImpl implements PublisherService {

     private final PublisherRepository publisherRepository;

     public PublisherServiceImpl(PublisherRepository publisherRepository) {
         this.publisherRepository = publisherRepository;
     }

    @Override
    public List<PublisherDTO> getAll() {
        return publisherRepository.findAll()
                .stream()
                .map(publisher -> new PublisherDTO(publisher.getId(), publisher.getPubName()))
                .toList();
    }

}
