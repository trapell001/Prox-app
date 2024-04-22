package com.prox.challenge.repository;

import com.prox.challenge.model.FormIcon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FormIconRepository extends JpaRepository<FormIcon, String> {
    List<FormIcon> findAllByRequiredVersionIsNullOrRequiredVersionLessThanEqual(int version);
}
