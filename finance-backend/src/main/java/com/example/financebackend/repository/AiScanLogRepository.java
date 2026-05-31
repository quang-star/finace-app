package com.example.financebackend.repository;

import com.example.financebackend.model.AiScanLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AiScanLogRepository extends JpaRepository<AiScanLog, Integer> {
    List<AiScanLog> findByUserUserId(Integer userId);
}
