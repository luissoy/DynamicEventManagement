package com.dynamiceventmanagement.databaselayer.service;

import com.dynamiceventmanagement.databaselayer.bean.CustomPropertiesBean;
import com.dynamiceventmanagement.databaselayer.dto.NotificationDto;
import com.dynamiceventmanagement.databaselayer.exception.DataIntegrityException;
import com.dynamiceventmanagement.databaselayer.exception.DataNotFoundException;
import com.dynamiceventmanagement.databaselayer.model.Notification;
import com.dynamiceventmanagement.databaselayer.repository.NotificationRepository;
import com.dynamiceventmanagement.databaselayer.response.PageResponse;
import com.dynamiceventmanagement.databaselayer.util.ServiceExceptionsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    @Autowired
    private NotificationRepository notificationRepository;

    public PageResponse<Notification> getAll(Pageable pageable) {
        return new PageResponse<>(notificationRepository.findAll(pageable));
    }

    public Notification getOne(String id) throws DataNotFoundException {
        return ServiceExceptionsUtil.
                getObjectOrDataNotFound(notificationRepository.findById(id));
    }

    public PageResponse<Notification> getAllByEventId(String eventId, Pageable pageable) {
        return new PageResponse<>(notificationRepository.findByEventId(eventId, pageable));
    }

    public Notification save(NotificationDto dto) throws DataIntegrityException {
        ServiceExceptionsUtil.notEmptyOrDataIntegrity(
                dto.getEventId(),
                CustomPropertiesBean.getProperty("exception.data.integrity.notification.event-id.empty"));

        return notificationRepository.save(new Notification(dto));
    }
}
