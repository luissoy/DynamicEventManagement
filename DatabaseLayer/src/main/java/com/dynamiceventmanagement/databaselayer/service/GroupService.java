package com.dynamiceventmanagement.databaselayer.service;

import com.dynamiceventmanagement.databaselayer.bean.CustomPropertiesBean;
import com.dynamiceventmanagement.databaselayer.dto.GroupDto;
import com.dynamiceventmanagement.databaselayer.exception.DataIntegrityException;
import com.dynamiceventmanagement.databaselayer.exception.DataNotFoundException;
import com.dynamiceventmanagement.databaselayer.model.Group;
import com.dynamiceventmanagement.databaselayer.repository.GroupRepository;
import com.dynamiceventmanagement.databaselayer.response.PageResponse;
import com.dynamiceventmanagement.databaselayer.util.ServiceExceptionsUtil;
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

    public PageResponse<Group> getByAppId(String appId, Pageable pageable) {
        return new PageResponse<>(groupRepository.findByAppId(appId, pageable));
    }

    public List<Group> getByAppId(String appId) {
        return groupRepository.findByAppId(appId);
    }

    public PageResponse<Group> getByUserId(String userId, Pageable pageable) {
        return new PageResponse<>(groupRepository.findGroupsByUserId(userId, pageable));
    }

    public List<Group> getByUserId(String userId) {
        return groupRepository.findGroupsByUserId(userId);
    }

    public Group save(GroupDto dto) throws DataIntegrityException {
        ServiceExceptionsUtil.notEmptyOrDataIntegrity(
                dto.getAppId(),
                CustomPropertiesBean.getProperty("exception.data.integrity.group.app-id.empty"));

        ServiceExceptionsUtil.notEmptyOrDataIntegrity(
                dto.getName(),
                CustomPropertiesBean.getProperty("exception.data.integrity.group.name.empty"));

        ServiceExceptionsUtil.notEmptyOrDataIntegrity(
                dto.getUserIds(),
                CustomPropertiesBean.getProperty("exception.data.integrity.group.user-ids.empty"));

        ServiceExceptionsUtil.noExistsOrDataIntegrity(
                groupRepository.existsByName(dto.getName()),
                CustomPropertiesBean.getProperty("exception.data.integrity.group.name.exists")
        );

        return groupRepository.save(new Group(dto));
    }

    public Group update(String id, GroupDto dto) throws DataNotFoundException, DataIntegrityException {
        Group oldGroup = ServiceExceptionsUtil.
                getObjectOrDataNotFound(
                        groupRepository.findById(id),
                        CustomPropertiesBean.getProperty("exception.data.not-found.group")
                );

        ServiceExceptionsUtil.noExistsOrDataIntegrity(
                groupRepository.existsByName(dto.getName()),
                CustomPropertiesBean.getProperty("exception.data.integrity.group.name.exists")
        );

        Group group = new Group(id,
                new GroupDto(
                        dto.getAppId() == null ? oldGroup.getAppId() : dto.getAppId(),
                        dto.getName() == null ? oldGroup.getName() : dto.getName(),
                        dto.getUserIds() == null ? oldGroup.getUserIds() : dto.getUserIds()
                ));

        return groupRepository.save(group);
    }

    public void delete(String id) throws DataNotFoundException {
        ServiceExceptionsUtil.existsOrDataNotFound(groupRepository.existsById(id));

        groupRepository.deleteById(id);
    }

}