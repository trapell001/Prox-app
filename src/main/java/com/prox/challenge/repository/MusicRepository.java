package com.prox.challenge.repository;

import com.prox.challenge.model.Music;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface MusicRepository extends JpaRepository<Music, String> {
}
