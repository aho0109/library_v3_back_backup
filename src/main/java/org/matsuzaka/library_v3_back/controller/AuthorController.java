package org.matsuzaka.library_v3_back.controller;


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
     * 查詢所有作者。
     * @return 所有作者列表
     */
    @GetMapping
    public Set<AuthorDTO> getAll() {
        Set<AuthorDTO> result = new LinkedHashSet<>(authorService.getAll());
        return result;
    }

    /**
     * 關鍵字搜尋作者（管理員）
     */
    @GetMapping("/search")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<AuthorDTO>> search(@RequestParam String keyword) {
        List<AuthorDTO> results = authorService.searchByKeyword(keyword);
        return ResponseEntity.ok(results);
    }

    /**
     * 新增作者（管理員）
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> create(@RequestBody AuthorDTO dto) {
        try {
            AuthorDTO created = authorService.create(dto);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 更新作者（管理員）
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody AuthorDTO dto) {
        try {
            AuthorDTO updated = authorService.update(id, dto);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 刪除作者（管理員）
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        try {
            authorService.delete(id);
            return ResponseEntity.ok("作者刪除成功");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
