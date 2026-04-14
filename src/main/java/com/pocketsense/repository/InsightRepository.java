package com.pocketsense.repository;

import com.pocketsense.model.Insight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface InsightRepository extends JpaRepository<Insight, UUID> {
}
