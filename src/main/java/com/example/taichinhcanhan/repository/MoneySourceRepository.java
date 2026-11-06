package com.example.taichinhcanhan.repository;

import com.example.taichinhcanhan.entity.MoneySource;
import com.example.taichinhcanhan.entity.MoneySourceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MoneySourceRepository extends JpaRepository<MoneySource, Long> {

          List<MoneySource> findByUserAccountIdOrderByNameAsc(Long userId);

          @Query("""
                              select source from MoneySource source
                              where source.userAccount.id = :userId
                                and (:keyword is null or :keyword = '' or lower(source.name) like lower(concat('%', :keyword, '%')))
                                and (:type is null or source.type = :type)
                              order by source.name asc
                              """)
          List<MoneySource> searchByUserId(
                              @Param("userId") Long userId,
                              @Param("keyword") String keyword,
                              @Param("type") MoneySourceType type);

          Optional<MoneySource> findByIdAndUserAccountId(Long id, Long userId);
}