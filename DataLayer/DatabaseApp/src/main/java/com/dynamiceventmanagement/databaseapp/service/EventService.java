package com.dynamiceventmanagement.databaseapp.service;

import com.dynamiceventmanagement.databaseapp.bean.CustomPropertiesBean;
import com.dynamiceventmanagement.databaseapp.dto.EventDto;
import com.dynamiceventmanagement.databaseapp.exception.DataIntegrityException;
import com.dynamiceventmanagement.databaseapp.exception.DataNotFoundException;
import com.dynamiceventmanagement.databaseapp.model.Event;
import com.dynamiceventmanagement.databaseapp.repository.EventRepository;
import com.dynamiceventmanagement.databaseapp.response.PageResponse;
import com.dynamiceventmanagement.databaseapp.util.ServiceExceptionsUtil;
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
