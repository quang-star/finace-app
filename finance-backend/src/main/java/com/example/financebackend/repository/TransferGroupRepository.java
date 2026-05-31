package com.example.financebackend.repository;

import com.example.financebackend.model.TransferGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TransferGroupRepository extends JpaRepository<TransferGroup, Integer> {
    List<TransferGroup> findByUserUserId(Integer userId);
}
