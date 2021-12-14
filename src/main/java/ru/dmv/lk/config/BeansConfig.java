package ru.dmv.lk.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.thymeleaf.TemplateEngine;

import java.util.Properties;

@Configuration
public class BeansConfig {

    @Bean
    public JavaMailSenderImpl getJavaMailSender() {
        return new JavaMailSenderImpl();
    }



}
