package com.prox.challenge.gcoder.repository;

import com.prox.challenge.gcoder.model.ConfigValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfigValueRepository extends JpaRepository<ConfigValue, String> {
}
