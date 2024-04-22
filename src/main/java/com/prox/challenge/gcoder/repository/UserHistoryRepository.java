package com.prox.challenge.gcoder.repository;

import com.prox.challenge.gcoder.model.UserHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserHistoryRepository extends JpaRepository<UserHistory, String> {
}
