package org.matsuzaka.library_v3_back.controller;

import org.matsuzaka.library_v3_back.dto.SeriesDTO;
import org.matsuzaka.library_v3_back.service.SeriesService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashSet;
import java.util.List;
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

    @GetMapping("/search")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<SeriesDTO>> search(@RequestParam String keyword) {
        List<SeriesDTO> results = seriesService.searchByKeyword(keyword);
        return ResponseEntity.ok(results);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> create(@RequestBody SeriesDTO dto) {
        try {
            SeriesDTO created = seriesService.create(dto);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody SeriesDTO dto) {
        try {
            SeriesDTO updated = seriesService.update(id, dto);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        try {
            seriesService.delete(id);
            return ResponseEntity.ok("系列刪除成功");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
