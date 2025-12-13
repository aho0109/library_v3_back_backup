package org.matsuzaka.library_v3_back.controller;

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

    @GetMapping("/search")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<PublisherDTO>> search(@RequestParam String keyword) {
        List<PublisherDTO> results = publisherService.searchByKeyword(keyword);
        return ResponseEntity.ok(results);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> create(@RequestBody PublisherDTO dto) {
        try {
            PublisherDTO created = publisherService.create(dto);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody PublisherDTO dto) {
        try {
            PublisherDTO updated = publisherService.update(id, dto);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        try {
            publisherService.delete(id);
            return ResponseEntity.ok("出版商刪除成功");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
