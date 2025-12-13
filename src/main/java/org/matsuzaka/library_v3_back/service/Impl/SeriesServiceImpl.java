package org.matsuzaka.library_v3_back.service.Impl;

import jakarta.persistence.EntityNotFoundException;
import org.matsuzaka.library_v3_back.dto.SeriesDTO;
import org.matsuzaka.library_v3_back.model.entity.Series;
import org.matsuzaka.library_v3_back.model.repositoryDao.SeriesRepository;
import org.matsuzaka.library_v3_back.service.SeriesService;
import org.springframework.stereotype.Service;

import java.util.HashSet;
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

    @Override
    public List<SeriesDTO> searchByKeyword(String keyword) {
        return seriesRepository.findByTitleContaining(keyword)
                .stream()
                .map(series -> new SeriesDTO(series.getId(), series.getTitle()))
                .toList();
    }

    @Override
    public SeriesDTO create(SeriesDTO dto) {
        // 檢查是否已存在
        if (seriesRepository.findByTitle(dto.getTitle()).isPresent()) {
            throw new IllegalArgumentException("系列已存在");
        }
        
        Series series = new Series();
        series.setTitle(dto.getTitle());
        Series saved = seriesRepository.save(series);
        
        return new SeriesDTO(saved.getId(), saved.getTitle());
    }

    @Override
    public SeriesDTO update(Long id, SeriesDTO dto) {
        Series series = seriesRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("系列不存在"));
        
        series.setTitle(dto.getTitle());
        Series updated = seriesRepository.save(series);
        
        return new SeriesDTO(updated.getId(), updated.getTitle());
    }

    @Override
    public void delete(Long id) {
        Series series = seriesRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("系列不存在"));
        
        // 檢查是否有書籍使用此系列
        if (!series.getBooks().isEmpty()) {
            throw new IllegalStateException("無法刪除有書籍的系列");
        }
        
        seriesRepository.delete(series);
    }

}
