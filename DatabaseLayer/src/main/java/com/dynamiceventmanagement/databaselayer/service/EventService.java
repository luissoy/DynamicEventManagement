package com.dynamiceventmanagement.databaselayer.service;

import com.dynamiceventmanagement.databaselayer.bean.CustomPropertiesBean;
import com.dynamiceventmanagement.databaselayer.dto.EventDto;
import com.dynamiceventmanagement.databaselayer.exception.DataIntegrityException;
import com.dynamiceventmanagement.databaselayer.exception.DataNotFoundException;
import com.dynamiceventmanagement.databaselayer.model.Event;
import com.dynamiceventmanagement.databaselayer.repository.EventRepository;
import com.dynamiceventmanagement.databaselayer.response.PageResponse;
import com.dynamiceventmanagement.databaselayer.util.ServiceExceptionsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class EventService {
    @Autowired
    private EventRepository eventRepository;

    public PageResponse<Event> getAll(Pageable pageable) {
        return new PageResponse<>(eventRepository.findAll(pageable));
    }

    public Event getOne(String id) throws DataNotFoundException {
        return ServiceExceptionsUtil.
                getObjectOrDataNotFound(
                        eventRepository.findById(id),
                        CustomPropertiesBean.getProperty("exception.data.not-found.event")
                );
    }

    public PageResponse<Event> getAllByGroupId(String groupId, Pageable pageable) {
        return new PageResponse<>(eventRepository.findByGroupId(groupId, pageable));
    }

    public Event save(EventDto dto) throws DataIntegrityException {
        validateDtoDataIntegrity(dto);

        if (dto.getDateTime() == null) {
            dto.setDateTime(LocalDateTime.now());
        }

        return eventRepository.save(new Event(dto));
    }

    private void validateDtoDataIntegrity(EventDto dto) throws DataIntegrityException {
        ServiceExceptionsUtil.notEmptyOrDataIntegrity(
                dto.getGroupId(),
                CustomPropertiesBean.getProperty("exception.data.integrity.event.group-id.empty"));

        ServiceExceptionsUtil.notEmptyOrDataIntegrity(
                dto.getUserId(),
                CustomPropertiesBean.getProperty("exception.data.integrity.event.user-id.empty"));
    }
}
