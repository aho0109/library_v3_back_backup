package org.matsuzaka.library_v3_back.controller;


import org.matsuzaka.library_v3_back.dto.AuthorDTO;
import org.matsuzaka.library_v3_back.service.AuthorService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashSet;
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

}
