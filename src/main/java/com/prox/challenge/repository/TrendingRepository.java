package com.prox.challenge.repository;

import com.prox.challenge.model.Trending;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrendingRepository extends JpaRepository<com.prox.challenge.model.Trending, String> {
    List<Trending> findAllByGroupAndTrendingType_RequiredVersionIsNullOrTrendingType_RequiredVersionLessThanEqualOrderByTopDesc(String group, int version);
    List<Trending> findAllByShowAndTrendingType_RequiredVersionIsNullOrTrendingType_RequiredVersionLessThanEqualOrderByTopDesc(boolean show, int version);
    List<Trending> findAllByShowAndGroupAndTrendingType_RequiredVersionIsNullOrTrendingType_RequiredVersionLessThanEqualOrderByTopDesc(boolean show, String group, int version);
    List<Trending> findAllByTrendingType_RequiredVersionIsNullOrTrendingType_RequiredVersionLessThanEqualOrderByTopDesc(int version);
}
