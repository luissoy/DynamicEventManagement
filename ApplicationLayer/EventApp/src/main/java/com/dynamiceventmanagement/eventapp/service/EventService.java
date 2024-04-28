package com.dynamiceventmanagement.eventapp.service;

import com.dynamiceventmanagement.eventapp.dto.EventDto;
import com.dynamiceventmanagement.eventapp.exception.DatabaseApiException;
import com.dynamiceventmanagement.eventapp.model.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EventService {
    @Autowired
    private EventApiService eventApiService;

    @Autowired
    private NotificationService notificationService;

    public Event save(String userId, String groupId, String appId, Object message) throws DatabaseApiException {
        EventDto eventDto = new EventDto(
                groupId,
                userId,
                null
        );

        Event event = eventApiService.save(eventDto);

        notificationService.sendNotifications(event.getId(), userId, groupId, appId, message);

        return event;
    }
}
