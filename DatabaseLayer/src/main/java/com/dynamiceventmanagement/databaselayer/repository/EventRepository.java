package com.dynamiceventmanagement.databaselayer.repository;

import com.dynamiceventmanagement.databaselayer.model.Event;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

@Repository
public interface EventRepository extends MongoRepository<Event, String> {
    Page<Event> findByGroupId(String groupId, Pageable pageable);
}