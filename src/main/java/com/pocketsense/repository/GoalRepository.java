package com.pocketsense.repository;

import com.pocketsense.model.Goal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

@Repository
public interface GoalRepository extends JpaRepository<Goal, UUID> {
    Optional<Goal> findByUserId(UUID userId);
    List<Goal> findAllByUserId(UUID userId);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Query("DELETE FROM Goal g WHERE g.userId = :userId")
    void deleteByUserId(UUID userId);
}
