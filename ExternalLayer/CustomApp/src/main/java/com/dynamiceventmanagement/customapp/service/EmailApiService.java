package com.dynamiceventmanagement.customapp.service;

import org.simplejavamail.api.email.Email;
import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.email.EmailBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailApiService {
    private final Mailer mailer;

    @Value("${api.mail.name}")
    private String mailName;

    @Value("${api.mail.email}")
    private String mailEmail;

    public EmailApiService(Mailer mailer) {
        this.mailer = mailer;
    }

    public void sendEmail(String to, String subject, String text) {
        Email email = EmailBuilder.startingBlank()
                .from(mailName, mailEmail)
                .to(to)
                .withSubject(subject)
                .withPlainText(text)
                .buildEmail();

        mailer.sendMail(email);
    }

}