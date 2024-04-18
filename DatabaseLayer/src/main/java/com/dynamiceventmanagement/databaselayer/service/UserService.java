package com.dynamiceventmanagement.databaselayer.service;

import com.dynamiceventmanagement.databaselayer.bean.CustomPropertiesBean;
import com.dynamiceventmanagement.databaselayer.bean.GroupDataDeletionBean;
import com.dynamiceventmanagement.databaselayer.dto.UserDto;
import com.dynamiceventmanagement.databaselayer.exception.DataIntegrityException;
import com.dynamiceventmanagement.databaselayer.exception.DataNotFoundException;
import com.dynamiceventmanagement.databaselayer.model.User;
import com.dynamiceventmanagement.databaselayer.repository.UserRepository;
import com.dynamiceventmanagement.databaselayer.response.PageResponse;
import com.dynamiceventmanagement.databaselayer.util.ServiceExceptionsUtil;
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
        ServiceExceptionsUtil.notEmptyOrDataIntegrity(
                dto.getUsername(),
                CustomPropertiesBean.getProperty("exception.data.integrity.user.username.empty"));

        ServiceExceptionsUtil.noExistsOrDataIntegrity(
                userRepository.existsByUsername(dto.getUsername()),
                CustomPropertiesBean.getProperty("exception.data.integrity.user.username.exists")
        );

        return userRepository.save(new User(dto));
    }

    public User update(String id, UserDto dto) throws DataNotFoundException, DataIntegrityException {
        User oldUser = ServiceExceptionsUtil.
                getObjectOrDataNotFound(
                        userRepository.findById(id),
                        CustomPropertiesBean.getProperty("exception.data.not-found.user")
                );

        ServiceExceptionsUtil.noExistsOrDataIntegrity(
                userRepository.existsByUsername(dto.getUsername()),
                CustomPropertiesBean.getProperty("exception.data.integrity.user.username.exists")
        );

        User user = new User(id,
                new UserDto(
                        dto.getUsername() == null ? oldUser.getUsername() : dto.getUsername(),
                        dto.getAppParameters() == null ? oldUser.getAppParameters() : dto.getAppParameters()
                ));

        return userRepository.save(user);
    }

    public void delete(String id) throws DataNotFoundException, DataIntegrityException {
        ServiceExceptionsUtil.existsOrDataNotFound(userRepository.existsById(id));

        groupDataDeletionBean.deleteUserFromGroupsByUserId(id);

        userRepository.deleteById(id);
    }

}