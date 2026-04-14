package com.pocketsense.repository;

import com.pocketsense.model.Saving;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
public interface SavingRepository extends JpaRepository<Saving, UUID> {
    List<Saving> findByUserId(UUID userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Saving s WHERE s.userId = :userId")
    void deleteByUserId(UUID userId);
}
