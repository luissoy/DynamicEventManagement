package com.dynamiceventmanagement.databaseapp.util;

import com.dynamiceventmanagement.databaseapp.exception.DataIntegrityException;
import com.dynamiceventmanagement.databaseapp.exception.DataNotFoundException;

import java.util.List;
import java.util.Optional;

public class ServiceExceptionsUtil {
    public static <T> T getObjectOrDataNotFound(
            Optional<T> optionalEntity,
            String errorMessage)
            throws DataNotFoundException {
        if (optionalEntity.isEmpty()) {
            throw new DataNotFoundException(errorMessage);
        }
        return optionalEntity.get();
    }

    public static void notEmptyOrDataIntegrity(String value, String errorMessage) throws DataIntegrityException {
        if (GeneralUtil.isNullOrEmpty(value)) {
            throw new DataIntegrityException(errorMessage);
        }
    }

    public static void notEmptyOrDataIntegrity(List<String> value, String errorMessage) throws DataIntegrityException {
        if (GeneralUtil.isNullOrEmpty(value)) {
            throw new DataIntegrityException(errorMessage);
        }
    }

    public static void existsOrDataNotFound(boolean exists, String errorMessage) throws DataNotFoundException {
        if (!exists) {
            throw new DataNotFoundException(errorMessage);
        }
    }

    public static void noExistsOrDataIntegrity(boolean exists, String errorMessage) throws DataIntegrityException {
        if (exists) {
            throw new DataIntegrityException(errorMessage);
        }
    }

}
