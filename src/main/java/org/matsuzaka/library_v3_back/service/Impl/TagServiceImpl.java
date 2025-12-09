package org.matsuzaka.library_v3_back.service.Impl;

import org.matsuzaka.library_v3_back.dto.TagDTO;
import org.matsuzaka.library_v3_back.dto.TagTop10DTO;
import org.matsuzaka.library_v3_back.model.entity.Tag;
import org.matsuzaka.library_v3_back.model.repositoryDao.TagRepository;
import org.matsuzaka.library_v3_back.service.TagService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

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
}
