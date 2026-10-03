package com.campusswap.service;

import com.campusswap.dto.NotificationResponse;
import com.campusswap.exception.ApiException;
import com.campusswap.model.Notification;
import com.campusswap.model.NotificationType;
import com.campusswap.model.User;
import com.campusswap.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final AuthService authService;

    public NotificationService(NotificationRepository notificationRepository, AuthService authService) {
        this.notificationRepository = notificationRepository;
        this.authService = authService;
    }

    /** Called by other services (ItemService, RequestService) inside their own transactions. */
    @Transactional
    public void create(User recipient, String title, String message,
                       NotificationType type, Long relatedRequestId) {
        Notification n = new Notification();
        n.setRecipient(recipient);
        n.setTitle(title);
        n.setMessage(truncate(message, 500));
        n.setType(type);
        n.setRelatedRequestId(relatedRequestId);
        notificationRepository.save(n);
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> listFor(String email) {
        User user = authService.requireUser(email);
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(NotificationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public long unreadCount(String email) {
        User user = authService.requireUser(email);
        return notificationRepository.countByRecipientIdAndReadFlagFalse(user.getId());
    }

    @Transactional
    public NotificationResponse markRead(String email, Long notificationId) {
        User user = authService.requireUser(email);
        Notification n = notificationRepository.findById(notificationId)
                .filter(x -> x.getRecipient().getId().equals(user.getId()))
                .orElseThrow(() -> ApiException.notFound("Notification not found."));
        n.setReadFlag(true);
        return NotificationResponse.from(notificationRepository.save(n));
    }

    @Transactional
    public int markAllRead(String email) {
        User user = authService.requireUser(email);
        List<Notification> unread = notificationRepository.findByRecipientIdAndReadFlagFalse(user.getId());
        unread.forEach(n -> n.setReadFlag(true));
        notificationRepository.saveAll(unread);
        return unread.size();
    }

    private String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }
}
