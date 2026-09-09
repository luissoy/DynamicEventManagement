package com.dynamiceventmanagement.emergencymapp;

import com.dynamiceventmanagement.emergencymapp.service.EmailApiService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class EmergencyAppApplicationTests {

	@Autowired
	EmailApiService emailApiService;

	/*@Test
	void contextLoads() {
	}*/

	@Test
	void testMain() {
		String to = "test@test.test";
		String subject = "Subject Test";
		String text = "Text Test";

		emailApiService.sendEmail(to, subject, text);
	}

}
