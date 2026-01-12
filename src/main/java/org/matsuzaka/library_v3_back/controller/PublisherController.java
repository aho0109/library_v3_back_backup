package org.matsuzaka.library_v3_back.controller;

import org.matsuzaka.library_v3_back.dto.common.ApiResponse;
import org.matsuzaka.library_v3_back.dto.PublisherDTO;
import org.matsuzaka.library_v3_back.service.PublisherService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/publishers")
@CrossOrigin(origins = "*")
public class PublisherController {

    private final PublisherService publisherService;

    public PublisherController(PublisherService publisherService) {
        this.publisherService = publisherService;
    }

    /**
     * 列出所有出版商
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Set<PublisherDTO>>> getAll() {
        Set<PublisherDTO> result = new LinkedHashSet<>(publisherService.getAll());
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<List<PublisherDTO>>> search(@RequestParam String keyword) {
        List<PublisherDTO> results = publisherService.searchByKeyword(keyword);
        return ResponseEntity.ok(ApiResponse.success(results));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<PublisherDTO>> create(@RequestBody PublisherDTO dto) {
        PublisherDTO created = publisherService.create(dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("出版商新增成功", created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<PublisherDTO>> update(@PathVariable Long id, @RequestBody PublisherDTO dto) {
        PublisherDTO updated = publisherService.update(id, dto);
        return ResponseEntity.ok(ApiResponse.success("出版商更新成功", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        publisherService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("出版商刪除成功"));
    }
}
