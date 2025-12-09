package org.matsuzaka.library_v3_back.controller;

import org.matsuzaka.library_v3_back.dto.PublisherDTO;
import org.matsuzaka.library_v3_back.service.PublisherService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashSet;
import java.util.Set;

@RestController
@RequestMapping("/api/publishers") // 統一的 publishers API 路徑
@CrossOrigin(origins = "*")
public class PublisherController {

    private final PublisherService publisherService;

    public PublisherController(PublisherService publisherService) {
        this.publisherService = publisherService;
    }

    /** * 列出所有出版商
     */
    @GetMapping
    public Set<PublisherDTO> getAll() {
        Set<PublisherDTO> result = new LinkedHashSet<>(publisherService.getAll());
        return result;
    }

}
