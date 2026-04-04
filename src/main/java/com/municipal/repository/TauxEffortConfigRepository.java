package com.municipal.repository;

import com.municipal.entity.TauxEffortConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TauxEffortConfigRepository extends JpaRepository<TauxEffortConfig, Long> {
}
