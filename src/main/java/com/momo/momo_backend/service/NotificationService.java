// src/main/java/com/momo/momo_backend/service/NotificationService.java
package com.momo.momo_backend.service;

import com.momo.momo_backend.dto.NotificationResponse;
import com.momo.momo_backend.entity.Notification;
import com.momo.momo_backend.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public List<NotificationResponse> getNotifications(Long userNo) {
        List<Notification> notifications = notificationRepository.findByReceiver_NoOrderByCreatedAtDesc(userNo);
        return notifications.stream()
                .map(NotificationResponse::fromEntity)
                .toList();
    }
}
