package com.dynamiceventmanagement.databaseapp.repository;

import com.dynamiceventmanagement.databaseapp.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByUsername(String username);

    @Query(value = "{'apps_parameters.?0': { $exists: true }}")
    List<User> findUsersWithByAppId(String appId);

    @Query(value = "{'apps_parameters.?0': { $exists: true }}")
    Page<User> findUsersWithByAppId(String appId, Pageable pageable);

    boolean existsByUsername(String username);
}