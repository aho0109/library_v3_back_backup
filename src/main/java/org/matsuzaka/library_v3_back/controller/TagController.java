package org.matsuzaka.library_v3_back.controller;

import org.matsuzaka.library_v3_back.dto.common.ApiResponse;
import org.matsuzaka.library_v3_back.dto.TagDTO;
import org.matsuzaka.library_v3_back.dto.TagTop10DTO;
import org.matsuzaka.library_v3_back.service.TagService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
     * 查詢熱門標籤前十名
     */
    @GetMapping({"/home/top10", "/home/top10/{categoryId}"})
    public ResponseEntity<ApiResponse<List<TagTop10DTO>>> getTop10(
            @PathVariable(value = "categoryId", required = false) Long categoryId) {
        List<TagTop10DTO> tags = tagService.getTop10(categoryId);
        return ResponseEntity.ok(ApiResponse.success(tags));
    }

    /**
     * 查詢所有標籤。
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Set<TagDTO>>> getAll() {
        Set<TagDTO> result = new LinkedHashSet<>(tagService.getAll());
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 根據關鍵字搜尋標籤。
     */
    @GetMapping("/search")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<List<TagDTO>>> search(@RequestParam String keyword) {
        List<TagDTO> results = tagService.searchByKeyword(keyword);
        return ResponseEntity.ok(ApiResponse.success(results));
    }

    /**
     * 新增標籤。
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<TagDTO>> create(@RequestBody TagDTO dto) {
        TagDTO created = tagService.create(dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("標籤新增成功", created));
    }

    /**
     * 更新標籤。
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<TagDTO>> update(@PathVariable Long id, @RequestBody TagDTO dto) {
        TagDTO updated = tagService.update(id, dto);
        return ResponseEntity.ok(ApiResponse.success("標籤更新成功", updated));
    }

    /**
     * 刪除標籤。
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        tagService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("標籤刪除成功"));
    }
}
