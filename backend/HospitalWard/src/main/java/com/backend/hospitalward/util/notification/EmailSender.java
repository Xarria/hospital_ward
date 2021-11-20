package com.backend.hospitalward.util.notification;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.simplejavamail.api.email.Email;
import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.api.mailer.config.TransportStrategy;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.MailerBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;


@FieldDefaults(level = AccessLevel.PRIVATE)
@Component
public class EmailSender {

    @Value("${mail.send}")
    String mailSend;
    @Value("${mail.username}")
    String username;
    @Value("${mail.password}")
    String password;
    @Value("${mail.address}")
    String address;
    @Value("${mail.from}")
    String from;

    void sendEmail(String recipientName, String recipientEmailAddress, String subject, String text) {
//        if (mailSend.equals("false")) {
//            return;
//        }

        Email email = EmailBuilder.startingBlank()
                .from(from, address)
                .to(recipientName, recipientEmailAddress)
                .withSubject(subject)
                .withHTMLText(text)
                .buildEmail();

        Mailer mailer = MailerBuilder
                .withSMTPServer("in-v3.mailjet.com", 587, username, password)
                .withTransportStrategy(TransportStrategy.SMTP)
                .buildMailer();

        mailer.sendMail(email);

    }

    public void sendModificationEmail(String recipientName, String recipientEmailAddress) {

        String htmlText = "test";
        String subject = "test";
        sendEmail(recipientName, recipientEmailAddress, subject, htmlText);

    }

    @Async
    public void sendAccountConfirmationEmails(String nameEmployee, String emailEmployee, String codeEmployee,
                                              String nameDirector, String emailDirector, String codeDirector) {

        String htmlTextEmployee = "test: " + codeEmployee;
        String htmlTextDirector = "testD: " + codeDirector;
        String subject = "Confirmation";

        sendEmail(nameEmployee, emailEmployee, subject, htmlTextEmployee);
        sendEmail(nameDirector, emailDirector, subject, htmlTextDirector);
    }

    public void sendPasswordResetEmails(String name, String emailEmployee, String codeEmployee,
                                        String emailDirector, String codeDirector) {
    }

    public void sendRemovalEmail(String name, String email) {
    }
}
