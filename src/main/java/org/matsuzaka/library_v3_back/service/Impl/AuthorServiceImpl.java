package org.matsuzaka.library_v3_back.service.Impl;

import org.matsuzaka.library_v3_back.dto.AuthorDTO;
import org.matsuzaka.library_v3_back.exception.BusinessException;
import org.matsuzaka.library_v3_back.exception.ErrorCode;
import org.matsuzaka.library_v3_back.exception.ResourceNotFoundException;
import org.matsuzaka.library_v3_back.model.entity.Author;
import org.matsuzaka.library_v3_back.model.repositoryDao.AuthorRepository;
import org.matsuzaka.library_v3_back.service.AuthorService;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;

@Service
public class AuthorServiceImpl implements AuthorService {

    private final AuthorRepository authorRepository;
    
    public AuthorServiceImpl(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    /**
     * 查詢所有作者。
     * @return 所有標籤列表
     */
    @Override
    public List<AuthorDTO> getAll() {
        return authorRepository.findAll()
                .stream()
                .map(author -> new AuthorDTO(author.getId(), author.getName()))
                .toList();
    }

    @Override
    public List<AuthorDTO> searchByKeyword(String keyword) {
        return authorRepository.findByNameContaining(keyword)
                .stream()
                .map(author -> new AuthorDTO(author.getId(), author.getName()))
                .toList();
    }

    @Override
    public AuthorDTO create(AuthorDTO dto) {
        // 檢查是否已存在
        if (authorRepository.findByName(dto.getName()).isPresent()) {
            throw new BusinessException(
                ErrorCode.AUTHOR_ALREADY_EXISTS, "作者: " + dto.getName());
        }
        
        Author author = new Author();
        author.setName(dto.getName());
        author.setBooks(new HashSet<>());
        Author saved = authorRepository.save(author);
        
        return new AuthorDTO(saved.getId(), saved.getName());
    }

    @Override
    public AuthorDTO update(Long id, AuthorDTO dto) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.AUTHOR_NOT_FOUND, "作者ID: " + id));

        author.setName(dto.getName());
        Author updated = authorRepository.save(author);
        
        return new AuthorDTO(updated.getId(), updated.getName());
    }

    @Override
    public void delete(Long id) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.AUTHOR_NOT_FOUND, "作者ID: " + id));

        // 檢查是否有書籍使用此作者
        if (!author.getBooks().isEmpty()) {
            throw new BusinessException(
                ErrorCode.AUTHOR_HAS_BOOKS);
        }

        authorRepository.delete(author);
    }

}
