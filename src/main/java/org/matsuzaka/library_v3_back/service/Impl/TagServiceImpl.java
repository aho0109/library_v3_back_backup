package org.matsuzaka.library_v3_back.service.Impl;

import org.matsuzaka.library_v3_back.dto.TagDTO;
import org.matsuzaka.library_v3_back.dto.TagTop10DTO;
import org.matsuzaka.library_v3_back.exception.BusinessException;
import org.matsuzaka.library_v3_back.exception.ErrorCode;
import org.matsuzaka.library_v3_back.exception.ResourceNotFoundException;
import org.matsuzaka.library_v3_back.model.entity.Tag;
import org.matsuzaka.library_v3_back.model.repositoryDao.TagRepository;
import org.matsuzaka.library_v3_back.service.TagService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;


@Service
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;
    public TagServiceImpl(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    /**
     * 查詢熱門標籤前十名。
     * @return 熱門標籤列表
     */
    @Override
    public List<TagTop10DTO> getTop10(Long categoryId) {
        Pageable pageable = PageRequest.of(0, 10);
        List<Tag> top10Tags = tagRepository.findTop10(categoryId, pageable);
        return top10Tags.stream().map(tag -> {
            TagTop10DTO dto = new TagTop10DTO();
            dto.setId(tag.getId());
            dto.setTitle(tag.getTitle());
            return dto;
        }).toList();
    }

    /**
     * 查詢所有標籤。
     *
     * @return 所有標籤列表
     */
    @Override
    public List<TagDTO> getAll() {
        return tagRepository.findAll()
                .stream()
                .map(tag -> new TagDTO(tag.getId(), tag.getTitle()))
                .toList();
    }

    @Override
    public List<TagDTO> searchByKeyword(String keyword) {
        return tagRepository.findByTitleContaining(keyword)
                .stream()
                .map(tag -> new TagDTO(tag.getId(), tag.getTitle()))
                .toList();
    }

    @Override
    public TagDTO create(TagDTO dto) {
        // 檢查是否已存在
        if (tagRepository.findByTitle(dto.getTitle()).isPresent()) {
            throw new BusinessException(ErrorCode.TAG_ALREADY_EXISTS, "標籤: " + dto.getTitle());
        }
        
        Tag tag = new Tag();
        tag.setTitle(dto.getTitle());
        tag.setBooks(new HashSet<>());
        Tag saved = tagRepository.save(tag);
        
        return new TagDTO(saved.getId(), saved.getTitle());
    }

    @Override
    public TagDTO update(Long id, TagDTO dto) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.TAG_NOT_FOUND, "標籤ID: " + id));

        tag.setTitle(dto.getTitle());
        Tag updated = tagRepository.save(tag);
        
        return new TagDTO(updated.getId(), updated.getTitle());
    }

    @Override
    public void delete(Long id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.TAG_NOT_FOUND, "標籤ID: " + id));

        // 解除雙向關聯：從每本書中移除此標籤，清空 tag 的 books 集合（不刪除書籍）
        tagRepository.deleteTagAssociations(tag.getId());
        
        tagRepository.delete(tag);
    }
}
