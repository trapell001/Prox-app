package com.prox.challenge.repository;

import com.prox.challenge.model.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ImageRepository extends JpaRepository<Image, String> {
    List<Image> findAllByType(String type);
}
