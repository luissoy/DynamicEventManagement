package com.dynamiceventmanagement.databaselayer.service;

import com.dynamiceventmanagement.databaselayer.bean.CustomPropertiesBean;
import com.dynamiceventmanagement.databaselayer.bean.GroupDataDeletionBean;
import com.dynamiceventmanagement.databaselayer.bean.UserDataDeletionBean;
import com.dynamiceventmanagement.databaselayer.dto.AppDto;
import com.dynamiceventmanagement.databaselayer.exception.DataIntegrityException;
import com.dynamiceventmanagement.databaselayer.exception.DataNotFoundException;
import com.dynamiceventmanagement.databaselayer.model.App;
import com.dynamiceventmanagement.databaselayer.repository.AppRepository;
import com.dynamiceventmanagement.databaselayer.response.PageResponse;
import com.dynamiceventmanagement.databaselayer.util.ServiceExceptionsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class AppService {

    @Autowired
    private AppRepository appRepository;

    @Autowired
    private GroupDataDeletionBean groupDataDeletionBean;

    @Autowired
    private UserDataDeletionBean userDataDeletionBean;

    public PageResponse<App> getAll(Pageable pageable) {
        return new PageResponse<>(appRepository.findAll(pageable));
    }

    public App getOne(String id) throws DataNotFoundException {
        return ServiceExceptionsUtil.
                getObjectOrDataNotFound(
                        appRepository.findById(id),
                        CustomPropertiesBean.getProperty("exception.data.not-found.app")
                );
    }

    public App getByNotificationUrl(String notificationUrl) throws DataNotFoundException {
        return ServiceExceptionsUtil.
                getObjectOrDataNotFound(
                        appRepository.findByNotificationUrl(notificationUrl),
                        CustomPropertiesBean.getProperty("exception.data.not-found.app.notification-url")
                );
    }

    public App save(AppDto dto) throws DataIntegrityException {
        validateDtoDataIntegrity(dto);

        return appRepository.save(new App(dto));
    }

    public App update(String id, AppDto dto) throws DataNotFoundException, DataIntegrityException {
        App oldApp = getOne(id);

        validateDtoDataIntegrity(dto);

        App app = new App(
                id,
                dto
        );

        return appRepository.save(app);
    }

    public void delete(String id) throws DataNotFoundException, DataIntegrityException {
        ServiceExceptionsUtil.existsOrDataNotFound(
                appRepository.existsById(id),
                CustomPropertiesBean.getProperty("exception.data.not-found.app")
        );

        groupDataDeletionBean.deleteGroupsByAppId(id);

        userDataDeletionBean.deleteAppParametersFromUsersByAppId(id);

        appRepository.deleteById(id);
    }

    private void validateDtoDataIntegrity(AppDto dto) throws DataIntegrityException {
        ServiceExceptionsUtil.notEmptyOrDataIntegrity(
                dto.getNotificationUrl(),
                CustomPropertiesBean.getProperty("exception.data.integrity.app.notification-url.empty")
        );
    }

}