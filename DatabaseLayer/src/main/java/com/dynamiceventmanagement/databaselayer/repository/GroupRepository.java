package com.dynamiceventmanagement.databaselayer.repository;

import com.dynamiceventmanagement.databaselayer.model.Group;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupRepository extends MongoRepository<Group, String> {

    List<Group> findByAppId(String appId);

    Page<Group> findByAppId(String appId, Pageable pageable);

    @Query("{'user_ids': { $in: [ ?0, '$user_ids' ] }}")
    List<Group> findGroupsByUserId(String userId);

    @Query("{'user_ids': { $in: [ ?0, '$user_ids' ] }}")
    Page<Group> findGroupsByUserId(String userId, Pageable pageable);

    boolean existsByName(String name);
}
