package com.municipal.repository;

import com.municipal.entity.PriceBracket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PriceBracketRepository extends JpaRepository<PriceBracket, Long> {
}
