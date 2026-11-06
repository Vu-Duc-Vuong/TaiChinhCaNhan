package com.example.taichinhcanhan.repository;

import com.example.taichinhcanhan.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {

          List<Budget> findByUserAccountIdOrderByStartDateDesc(Long userId);

          Optional<Budget> findByIdAndUserAccountId(Long id, Long userId);

          @Query("""
                              select budget from Budget budget
                              where budget.userAccount.id = :userId
                                and (:keyword is null or :keyword = '' or lower(budget.name) like lower(concat('%', :keyword, '%')))
                                and (:categoryId is null or budget.category.id = :categoryId)
                                and (:overall is null
                                 or (:overall = true and budget.category is null)
                                 or (:overall = false and budget.category is not null))
                                and (:startDate is null or budget.endDate >= :startDate)
                                and (:endDate is null or budget.startDate <= :endDate)
                              order by budget.startDate desc, budget.id desc
                              """)
          List<Budget> searchByUserId(
                              @Param("userId") Long userId,
                              @Param("keyword") String keyword,
                              @Param("categoryId") Long categoryId,
                              @Param("overall") Boolean overall,
                              @Param("startDate") LocalDate startDate,
                              @Param("endDate") LocalDate endDate);

          @Query("""
                              select case when count(budget) > 0 then true else false end
                              from Budget budget
                              where budget.userAccount.id = :userId
                                and ((:categoryId is null and budget.category is null)
                                 or (:categoryId is not null and budget.category.id = :categoryId))
                                and budget.startDate <= :endDate
                                and budget.endDate >= :startDate
                                and (:excludeId is null or budget.id <> :excludeId)
                              """)
          boolean existsOverlappingBudget(
                              @Param("userId") Long userId,
                              @Param("categoryId") Long categoryId,
                              @Param("startDate") LocalDate startDate,
                              @Param("endDate") LocalDate endDate,
                              @Param("excludeId") Long excludeId);

          boolean existsByCategoryId(Long categoryId);
}