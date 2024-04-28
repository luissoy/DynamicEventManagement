package com.dynamiceventmanagement.databaseapp.repository;

import com.dynamiceventmanagement.databaseapp.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface NotificationRepository extends MongoRepository<Notification, String> {
    Page<Notification> findByEventId(String eventId, Pageable pageable);
}
