package com.prox.challenge.gcoder.repository;

import com.prox.challenge.gcoder.model.UserSimple;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserSimpleRepository extends JpaRepository<UserSimple, String> {
    Optional<UserSimple> findByEmail(String email);

}
