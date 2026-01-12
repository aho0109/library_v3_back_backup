package org.matsuzaka.library_v3_back.controller;

import org.matsuzaka.library_v3_back.dto.common.ApiResponse;
import org.matsuzaka.library_v3_back.dto.AuthorDTO;
import org.matsuzaka.library_v3_back.service.AuthorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/authors")
@CrossOrigin(origins = "*")
public class AuthorController {

    private final AuthorService authorService;

    public AuthorController(AuthorService authorService) {
        this.authorService = authorService;
    }

    /**
     * 查詢所有作者
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Set<AuthorDTO>>> getAll() {
        Set<AuthorDTO> result = new LinkedHashSet<>(authorService.getAll());
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 關鍵字搜尋作者（管理員）
     */
    @GetMapping("/search")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<List<AuthorDTO>>> search(@RequestParam String keyword) {
        List<AuthorDTO> results = authorService.searchByKeyword(keyword);
        return ResponseEntity.ok(ApiResponse.success(results));
    }

    /**
     * 新增作者（管理員）
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<AuthorDTO>> create(@RequestBody AuthorDTO dto) {
        AuthorDTO created = authorService.create(dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("作者新增成功", created));
    }

    /**
     * 更新作者（管理員）
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<AuthorDTO>> update(@PathVariable Long id, @RequestBody AuthorDTO dto) {
        AuthorDTO updated = authorService.update(id, dto);
        return ResponseEntity.ok(ApiResponse.success("作者更新成功", updated));
    }

    /**
     * 刪除作者（管理員）
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        authorService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("作者刪除成功"));
    }
}
