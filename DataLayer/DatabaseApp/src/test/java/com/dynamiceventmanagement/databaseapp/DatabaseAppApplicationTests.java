package com.dynamiceventmanagement.databaseapp;

import com.dynamiceventmanagement.databaseapp.model.App;
import com.dynamiceventmanagement.databaseapp.repository.AppRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
class DatabaseAppApplicationTests {
    @Autowired
    private AppRepository appRepository;

    @Test
    void contextLoads() {
    }

    @Test
    void testDataBaseConnectivity() {
        // Given
        String id = "1";
        App app = new App(id, "App name");

        // When

        // Then
        appRepository.save(app);

        Optional<App> savedUserOptional = appRepository.findById(id);
        assertTrue(savedUserOptional.isPresent());
        assertEquals(app.getId(), savedUserOptional.get().getId());
        assertEquals(app.getNotificationUrl(), savedUserOptional.get().getNotificationUrl());

        appRepository.deleteById(id);
    }

}
