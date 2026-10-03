package com.campusswap.repository;

import com.campusswap.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByRecipientIdOrderByCreatedAtDesc(Long recipientId);

    List<Notification> findByRecipientIdAndReadFlagFalse(Long recipientId);

    long countByRecipientIdAndReadFlagFalse(Long recipientId);
}
