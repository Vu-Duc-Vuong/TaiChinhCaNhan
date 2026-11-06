package com.example.taichinhcanhan.repository;

import com.example.taichinhcanhan.entity.Category;
import com.example.taichinhcanhan.entity.CategoryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

          List<Category> findByUserAccountIdOrderByNameAsc(Long userId);

          List<Category> findByUserAccountIdAndTypeOrderByNameAsc(Long userId, CategoryType type);

          @Query("""
                              select category from Category category
                              where category.userAccount.id = :userId
                                and (:keyword is null or :keyword = '' or lower(category.name) like lower(concat('%', :keyword, '%')))
                                and (:type is null or category.type = :type)
                              order by category.name asc
                              """)
          List<Category> searchByUserId(
                              @Param("userId") Long userId,
                              @Param("keyword") String keyword,
                              @Param("type") CategoryType type);

          boolean existsByUserAccountIdAndNameIgnoreCaseAndType(Long userId, String name, CategoryType type);

          boolean existsByUserAccountIdAndNameIgnoreCaseAndTypeAndIdNot(
                              Long userId,
                              String name,
                              CategoryType type,
                              Long id);

          Optional<Category> findByIdAndUserAccountId(Long id, Long userId);
}