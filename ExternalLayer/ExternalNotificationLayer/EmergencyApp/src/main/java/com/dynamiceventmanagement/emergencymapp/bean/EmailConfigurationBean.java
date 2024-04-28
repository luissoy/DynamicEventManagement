package com.dynamiceventmanagement.emergencymapp.bean;

import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.api.mailer.config.TransportStrategy;
import org.simplejavamail.mailer.MailerBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmailConfigurationBean {
    @Value("${api.mail.host}")
    private String mailHost;

    @Value("${api.mail.port}")
    private int mailPort;

    @Bean
    public Mailer mailerTemplate() {

        return MailerBuilder
                .withSMTPServer(mailHost, mailPort)
                .withTransportStrategy(TransportStrategy.SMTP)
                .buildMailer();
    }
}
