package com.dynamiceventmanagement.databaseapp.service;

import com.dynamiceventmanagement.databaseapp.bean.CustomPropertiesBean;
import com.dynamiceventmanagement.databaseapp.bean.UserDataDeletionBean;
import com.dynamiceventmanagement.databaseapp.dto.AppDto;
import com.dynamiceventmanagement.databaseapp.exception.DataIntegrityException;
import com.dynamiceventmanagement.databaseapp.exception.DataNotFoundException;
import com.dynamiceventmanagement.databaseapp.model.App;
import com.dynamiceventmanagement.databaseapp.repository.AppRepository;
import com.dynamiceventmanagement.databaseapp.response.PageResponse;
import com.dynamiceventmanagement.databaseapp.util.ServiceExceptionsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class AppService {

    @Autowired
    private AppRepository appRepository;

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