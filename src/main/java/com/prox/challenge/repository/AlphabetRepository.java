package com.prox.challenge.repository;

import com.prox.challenge.model.Alphabet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlphabetRepository extends JpaRepository<Alphabet, Long> {
}
