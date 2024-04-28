package com.dynamiceventmanagement.databaseapp.service;

import com.dynamiceventmanagement.databaseapp.bean.CustomPropertiesBean;
import com.dynamiceventmanagement.databaseapp.dto.GroupDto;
import com.dynamiceventmanagement.databaseapp.exception.DataIntegrityException;
import com.dynamiceventmanagement.databaseapp.exception.DataNotFoundException;
import com.dynamiceventmanagement.databaseapp.model.Group;
import com.dynamiceventmanagement.databaseapp.repository.GroupRepository;
import com.dynamiceventmanagement.databaseapp.response.PageResponse;
import com.dynamiceventmanagement.databaseapp.util.ServiceExceptionsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GroupService {

    @Autowired
    private GroupRepository groupRepository;

    public PageResponse<Group> getAll(Pageable pageable) {
        return new PageResponse<>(groupRepository.findAll(pageable));
    }

    public Group getOne(String id) throws DataNotFoundException {
        return ServiceExceptionsUtil.
                getObjectOrDataNotFound(
                        groupRepository.findById(id),
                        CustomPropertiesBean.getProperty("exception.data.not-found.group")
                );
    }

    public PageResponse<Group> getByName(String name, Pageable pageable) {
        return new PageResponse<>(groupRepository.findByName(name, pageable));
    }

    public PageResponse<Group> getByUserId(String userId, Pageable pageable) {
        return new PageResponse<>(groupRepository.findGroupsByUserId(userId, pageable));
    }

    public List<Group> getByUserId(String userId) {
        return groupRepository.findGroupsByUserId(userId);
    }

    public Group save(GroupDto dto) throws DataIntegrityException {
        validateDtoDataIntegrity(dto);

        return groupRepository.save(new Group(dto));
    }

    public Group update(String id, GroupDto dto) throws DataNotFoundException, DataIntegrityException {
        Group old = getOne(id);

        validateDtoDataIntegrity(dto);

        Group group = new Group(
                id,
                dto
        );

        return groupRepository.save(group);
    }

    public void delete(String id) throws DataNotFoundException {
        ServiceExceptionsUtil.existsOrDataNotFound(
                groupRepository.existsById(id),
                CustomPropertiesBean.getProperty("exception.data.not-found.group")
        );

        groupRepository.deleteById(id);
    }

    private void validateDtoDataIntegrity(GroupDto dto) throws DataIntegrityException {
        ServiceExceptionsUtil.notEmptyOrDataIntegrity(
                dto.getName(),
                CustomPropertiesBean.getProperty("exception.data.integrity.group.name.empty")
        );

        ServiceExceptionsUtil.notEmptyOrDataIntegrity(
                dto.getUserIds(),
                CustomPropertiesBean.getProperty("exception.data.integrity.group.user-ids.empty")
        );
    }

}