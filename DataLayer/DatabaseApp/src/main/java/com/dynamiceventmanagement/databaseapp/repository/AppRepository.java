package com.dynamiceventmanagement.databaseapp.repository;

import com.dynamiceventmanagement.databaseapp.model.App;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AppRepository extends MongoRepository<App, String> {
    Optional<App> findByNotificationUrl(String name);
}