package org.matsuzaka.library_v3_back.controller;

import org.matsuzaka.library_v3_back.dto.SeriesDTO;
import org.matsuzaka.library_v3_back.service.SeriesService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashSet;
import java.util.Set;

@RestController
@RequestMapping("/api/series")
@CrossOrigin(origins = "*")
public class SeriesController {

    private final SeriesService seriesService;
    public SeriesController(SeriesService seriesService) {
        this.seriesService = seriesService;
    }

    /**
     * 獲取所有系列的列表
     * @return 包含所有系列的 DTO 列表
     */
    @GetMapping
    public Set<SeriesDTO> getAll() {
        Set<SeriesDTO> result = new LinkedHashSet<>(seriesService.getAll());
        return result;
    }

}
