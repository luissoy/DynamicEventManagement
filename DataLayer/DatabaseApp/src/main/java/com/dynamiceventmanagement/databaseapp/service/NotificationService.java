package com.dynamiceventmanagement.databaseapp.service;

import com.dynamiceventmanagement.databaseapp.bean.CustomPropertiesBean;
import com.dynamiceventmanagement.databaseapp.dto.NotificationDto;
import com.dynamiceventmanagement.databaseapp.exception.DataIntegrityException;
import com.dynamiceventmanagement.databaseapp.exception.DataNotFoundException;
import com.dynamiceventmanagement.databaseapp.model.Notification;
import com.dynamiceventmanagement.databaseapp.repository.NotificationRepository;
import com.dynamiceventmanagement.databaseapp.response.PageResponse;
import com.dynamiceventmanagement.databaseapp.util.ServiceExceptionsUtil;
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
                getObjectOrDataNotFound(
                        notificationRepository.findById(id),
                        CustomPropertiesBean.getProperty("exception.data.not-found.notification")
                );
    }

    public PageResponse<Notification> getAllByEventId(String eventId, Pageable pageable) {
        return new PageResponse<>(notificationRepository.findByEventId(eventId, pageable));
    }

    public Notification save(NotificationDto dto) throws DataIntegrityException {
        validateDtoDataIntegrity(dto);

        return notificationRepository.save(new Notification(dto));
    }

    private void validateDtoDataIntegrity(NotificationDto dto) throws DataIntegrityException {
        ServiceExceptionsUtil.notEmptyOrDataIntegrity(
                dto.getEventId(),
                CustomPropertiesBean.getProperty("exception.data.integrity.notification.event-id.empty"));

        ServiceExceptionsUtil.notEmptyOrDataIntegrity(
                dto.getAppId(),
                CustomPropertiesBean.getProperty("exception.data.integrity.notification.app-id.empty"));

        ServiceExceptionsUtil.notEmptyOrDataIntegrity(
                dto.getUser().getId(),
                CustomPropertiesBean.getProperty("exception.data.integrity.notification.user-id.empty"));

        ServiceExceptionsUtil.notEmptyOrDataIntegrity(
                dto.getUserToNotify().getId(),
                CustomPropertiesBean.getProperty("exception.data.integrity.notification.user-id-to-notify.empty"));
    }
}
