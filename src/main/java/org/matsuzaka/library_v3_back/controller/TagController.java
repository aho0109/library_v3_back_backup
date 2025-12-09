package org.matsuzaka.library_v3_back.controller;

import org.matsuzaka.library_v3_back.dto.TagDTO;
import org.matsuzaka.library_v3_back.dto.TagTop10DTO;
import org.matsuzaka.library_v3_back.service.TagService;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/tags")
@CrossOrigin(origins = "*")
public class TagController {

    private final TagService tagService;
    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    /**
     * 查詢熱門標籤前十名。
     * @return 熱門標籤列表
     */
    @GetMapping({"/home/top10", "/home/top10/{categoryId}"})
    public List<TagTop10DTO> getTop10(@PathVariable(value = "categoryId", required = false) Long categoryId) {
        return tagService.getTop10(categoryId);
    }

    /**
     * 查詢所有標籤。
     * @return 所有標籤列表
     */
    @GetMapping
    public Set<TagDTO> getAll() {
        Set<TagDTO> result = new LinkedHashSet<>(tagService.getAll());
        return result;
    }

}
