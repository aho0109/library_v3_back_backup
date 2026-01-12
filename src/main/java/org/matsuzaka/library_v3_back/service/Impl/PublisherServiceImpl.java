package org.matsuzaka.library_v3_back.service.Impl;

import org.matsuzaka.library_v3_back.dto.PublisherDTO;
import org.matsuzaka.library_v3_back.exception.BusinessException;
import org.matsuzaka.library_v3_back.exception.ErrorCode;
import org.matsuzaka.library_v3_back.exception.ResourceNotFoundException;
import org.matsuzaka.library_v3_back.model.entity.Publisher;
import org.matsuzaka.library_v3_back.model.repositoryDao.PublisherRepository;
import org.matsuzaka.library_v3_back.service.PublisherService;
import org.springframework.stereotype.Service;

import java.util.HashSet;
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

    @Override
    public List<PublisherDTO> searchByKeyword(String keyword) {
        return publisherRepository.findByPubNameContaining(keyword)
                .stream()
                .map(publisher -> new PublisherDTO(publisher.getId(), publisher.getPubName()))
                .toList();
    }

    @Override
    public PublisherDTO create(PublisherDTO dto) {
        // 檢查是否已存在
        if (publisherRepository.findByPubName(dto.getPubName()).isPresent()) {
            throw new BusinessException(ErrorCode.PUBLISHER_ALREADY_EXISTS, "出版社: " + dto.getPubName());
        }
        
        Publisher publisher = new Publisher();
        publisher.setPubName(dto.getPubName());
        Publisher saved = publisherRepository.save(publisher);
        
        return new PublisherDTO(saved.getId(), saved.getPubName());
    }

    @Override
    public PublisherDTO update(Long id, PublisherDTO dto) {
        Publisher publisher = publisherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PUBLISHER_NOT_FOUND, "出版商ID: " + id));

        publisher.setPubName(dto.getPubName());
        Publisher updated = publisherRepository.save(publisher);
        
        return new PublisherDTO(updated.getId(), updated.getPubName());
    }

    @Override
    public void delete(Long id) {
        Publisher publisher = publisherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PUBLISHER_NOT_FOUND, "出版商ID: " + id));

        // 檢查是否有書籍使用此出版商
        if (!publisher.getBooks().isEmpty()) {
            throw new BusinessException(ErrorCode.PUBLISHER_HAS_BOOKS);
        }
        
        publisherRepository.delete(publisher);
    }

}
