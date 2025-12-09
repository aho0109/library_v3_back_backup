package org.matsuzaka.library_v3_back.service.Impl;

import org.matsuzaka.library_v3_back.dto.SeriesDTO;
import org.matsuzaka.library_v3_back.model.repositoryDao.SeriesRepository;
import org.matsuzaka.library_v3_back.service.SeriesService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SeriesServiceImpl implements SeriesService {

    private SeriesRepository seriesRepository;
    public SeriesServiceImpl(SeriesRepository seriesRepository) {
        this.seriesRepository = seriesRepository;
    }

    /**
     * 獲取所有系列的列表
     * @return 包含所有系列的 DTO 列表
     */
    @Override
    public List<SeriesDTO> getAll() {
        return seriesRepository.findAll()
                .stream()
                .map(series -> new SeriesDTO(series.getId(), series.getTitle()))
                .toList();
    }

}
