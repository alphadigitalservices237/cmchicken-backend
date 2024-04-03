package com.limiter.demo.rest.controllers;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendEmail(String to, String subject, String body) throws MessagingException {
        SimpleMailMessage message = new SimpleMailMessage();
        MimeMessage msg = mailSender.createMimeMessage();
        String htmlContent = body;
        msg.setFrom("alphadigitalservices237@gmail.com");
        msg.setRecipients(MimeMessage.RecipientType.TO, to);
        msg.setSubject(subject);
        msg.setContent(htmlContent, "text/html; charset=utf-8");
        mailSender.send(message);
    }
}
