package ru.dmv.lk.service;

import org.springframework.mail.MailException;

import javax.mail.MessagingException;
import java.util.Map;

public interface EmailService {
    // Отправить письмо с Html или без
    void send(String to, String title, String body, boolean isHtml);
}