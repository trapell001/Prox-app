package com.prox.challenge.repository;

import com.prox.challenge.model.TrendingType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrendingTypeRepository extends JpaRepository<TrendingType, String> {
    List<TrendingType> findAllByRequiredVersionIsNullOrRequiredVersionLessThanEqualOrderByRank(int version);
}
