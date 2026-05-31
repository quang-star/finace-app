package com.example.financebackend.repository;

import com.example.financebackend.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    List<Notification> findByUserUserIdAndIsReadFalse(Integer userId);
    List<Notification> findByUserUserId(Integer userId);
}
