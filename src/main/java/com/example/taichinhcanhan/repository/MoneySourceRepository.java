package com.example.taichinhcanhan.repository;

import com.example.taichinhcanhan.entity.MoneySource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MoneySourceRepository extends JpaRepository<MoneySource, Long> {

          List<MoneySource> findByUserAccountIdOrderByNameAsc(Long userId);

          Optional<MoneySource> findByIdAndUserAccountId(Long id, Long userId);
}