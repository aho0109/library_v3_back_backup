package org.matsuzaka.library_v3_back.controller;

import org.matsuzaka.library_v3_back.dto.common.ApiResponse;
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
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Set<SeriesDTO>>> getAll() {
        Set<SeriesDTO> result = new LinkedHashSet<>(seriesService.getAll());
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 根據關鍵字搜尋系列
     */
    @GetMapping("/search")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<List<SeriesDTO>>> search(@RequestParam String keyword) {
        List<SeriesDTO> results = seriesService.searchByKeyword(keyword);
        return ResponseEntity.ok(ApiResponse.success(results));
    }

    /**
     * 新增系列
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<SeriesDTO>> create(@RequestBody SeriesDTO dto) {
        SeriesDTO created = seriesService.create(dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("系列新增成功", created));
    }

    /**
     * 更新系列
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<SeriesDTO>> update(@PathVariable Long id, @RequestBody SeriesDTO dto) {
        SeriesDTO updated = seriesService.update(id, dto);
        return ResponseEntity.ok(ApiResponse.success("系列更新成功", updated));
    }

    /**
     * 刪除系列
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        seriesService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("系列刪除成功"));
    }
}
