package com.example.financebackend.repository;

import com.example.financebackend.model.AiProductLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AiProductLogRepository extends JpaRepository<AiProductLog, Integer> {
    List<AiProductLog> findByUserUserId(Integer userId);
}
