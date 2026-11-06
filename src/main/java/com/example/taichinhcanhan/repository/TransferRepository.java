package com.example.taichinhcanhan.repository;

import com.example.taichinhcanhan.entity.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TransferRepository extends JpaRepository<Transfer, Long> {

          @Query("""
                              select transfer from Transfer transfer
                              where transfer.userAccount.id = :userId
                                and (:keyword is null or :keyword = '' or lower(transfer.description) like lower(concat('%', :keyword, '%')))
                                and (:fromSourceId is null or transfer.fromSource.id = :fromSourceId)
                                and (:toSourceId is null or transfer.toSource.id = :toSourceId)
                                and (:startDate is null or transfer.transferDate >= :startDate)
                                and (:endDate is null or transfer.transferDate <= :endDate)
                              order by transfer.transferDate desc, transfer.id desc
                              """)
          List<Transfer> searchByUserId(
                              @Param("userId") Long userId,
                              @Param("keyword") String keyword,
                              @Param("fromSourceId") Long fromSourceId,
                              @Param("toSourceId") Long toSourceId,
                              @Param("startDate") LocalDate startDate,
                              @Param("endDate") LocalDate endDate);

          Optional<Transfer> findByIdAndUserAccountId(Long id, Long userId);

          boolean existsByFromSourceIdOrToSourceId(Long fromSourceId, Long toSourceId);

          @Query("select transfer.fromSource.name, coalesce(sum(transfer.amount), 0) "
                              + "from Transfer transfer where transfer.userAccount.id = :userId "
                              + "and transfer.transferDate between :startDate and :endDate "
                              + "group by transfer.fromSource.id, transfer.fromSource.name")
          List<Object[]> summarizeOutgoingBySource(
                              @Param("userId") Long userId,
                              @Param("startDate") LocalDate startDate,
                              @Param("endDate") LocalDate endDate);

          @Query("select transfer.toSource.name, coalesce(sum(transfer.amount), 0) "
                              + "from Transfer transfer where transfer.userAccount.id = :userId "
                              + "and transfer.transferDate between :startDate and :endDate "
                              + "group by transfer.toSource.id, transfer.toSource.name")
          List<Object[]> summarizeIncomingBySource(
                              @Param("userId") Long userId,
                              @Param("startDate") LocalDate startDate,
                              @Param("endDate") LocalDate endDate);
}