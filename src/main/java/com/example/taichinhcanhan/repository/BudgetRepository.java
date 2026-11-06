package com.example.taichinhcanhan.repository;

import com.example.taichinhcanhan.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {

          List<Budget> findByUserAccountIdOrderByStartDateDesc(Long userId);

          Optional<Budget> findByIdAndUserAccountId(Long id, Long userId);

          boolean existsByCategoryId(Long categoryId);
}