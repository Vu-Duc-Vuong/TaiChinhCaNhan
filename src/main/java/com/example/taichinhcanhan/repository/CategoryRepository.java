package com.example.taichinhcanhan.repository;

import com.example.taichinhcanhan.entity.Category;
import com.example.taichinhcanhan.entity.CategoryType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

          List<Category> findByUserAccountIdOrderByNameAsc(Long userId);

          List<Category> findByUserAccountIdAndTypeOrderByNameAsc(Long userId, CategoryType type);

          Optional<Category> findByIdAndUserAccountId(Long id, Long userId);
}