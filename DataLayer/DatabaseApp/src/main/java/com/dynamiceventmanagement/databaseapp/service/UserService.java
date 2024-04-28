package com.dynamiceventmanagement.databaseapp.service;

import com.dynamiceventmanagement.databaseapp.bean.CustomPropertiesBean;
import com.dynamiceventmanagement.databaseapp.bean.GroupDataDeletionBean;
import com.dynamiceventmanagement.databaseapp.dto.UserDto;
import com.dynamiceventmanagement.databaseapp.exception.DataIntegrityException;
import com.dynamiceventmanagement.databaseapp.exception.DataNotFoundException;
import com.dynamiceventmanagement.databaseapp.model.User;
import com.dynamiceventmanagement.databaseapp.repository.UserRepository;
import com.dynamiceventmanagement.databaseapp.response.PageResponse;
import com.dynamiceventmanagement.databaseapp.util.ServiceExceptionsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupDataDeletionBean groupDataDeletionBean;

    public PageResponse<User> getAll(Pageable pageable) {
        return new PageResponse<>(userRepository.findAll(pageable));
    }

    public User getOne(String id) throws DataNotFoundException {
        return ServiceExceptionsUtil.
                getObjectOrDataNotFound(
                        userRepository.findById(id),
                        CustomPropertiesBean.getProperty("exception.data.not-found.user")
                );
    }

    public User getByUsername(String username) throws DataNotFoundException {
        return ServiceExceptionsUtil.
                getObjectOrDataNotFound(
                        userRepository.findByUsername(username),
                        CustomPropertiesBean.getProperty("exception.data.not-found.user.username")
                );
    }

    public PageResponse<User> getByAppId(String appId, Pageable pageable) {
        return new PageResponse<>(userRepository.findUsersWithByAppId(appId, pageable));
    }

    public List<User> getByAppId(String appId) {
        return userRepository.findUsersWithByAppId(appId);
    }

    public User save(UserDto dto) throws DataIntegrityException {
        validateDtoDataIntegrity(dto);

        return userRepository.save(new User(dto));
    }

    public User update(String id, UserDto dto) throws DataNotFoundException, DataIntegrityException {
        User old = getOne(id);

        validateDtoDataIntegrity(dto, old);

        User user = new User(
                id,
                dto
        );

        return userRepository.save(user);
    }

    public void delete(String id) throws DataNotFoundException, DataIntegrityException {
        ServiceExceptionsUtil.existsOrDataNotFound(
                userRepository.existsById(id),
                CustomPropertiesBean.getProperty("exception.data.not-found.user")
        );

        groupDataDeletionBean.deleteUserFromGroupsByUserId(id);

        userRepository.deleteById(id);
    }

    private void validateDtoDataIntegrity(UserDto dto) throws DataIntegrityException {
        ServiceExceptionsUtil.notEmptyOrDataIntegrity(
                dto.getUsername(),
                CustomPropertiesBean.getProperty("exception.data.integrity.user.username.empty"));

        ServiceExceptionsUtil.noExistsOrDataIntegrity(
                userRepository.existsByUsername(dto.getUsername()),
                CustomPropertiesBean.getProperty("exception.data.integrity.user.username.exists")
        );
    }

    private void validateDtoDataIntegrity(UserDto dto, User old) throws DataIntegrityException {
        ServiceExceptionsUtil.notEmptyOrDataIntegrity(
                dto.getUsername(),
                CustomPropertiesBean.getProperty("exception.data.integrity.user.username.empty"));

        if (!dto.getUsername().equals(old.getUsername())) {
            ServiceExceptionsUtil.noExistsOrDataIntegrity(
                    userRepository.existsByUsername(dto.getUsername()),
                    CustomPropertiesBean.getProperty("exception.data.integrity.user.username.exists")
            );
        }
    }

}