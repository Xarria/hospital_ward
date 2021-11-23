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

    @Value("${mail.username}")
    String username;
    @Value("${mail.password}")
    String password;
    @Value("${mail.address}")
    String address;
    @Value("${mail.from}")
    String from;

    void sendEmail(String recipientName, String recipientEmailAddress, String subject, String text) {

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

        String htmlText = "Twoje konto zostało zmodyfikowane";
        String subject = "Modyfikacja danych konta";
        sendEmail(recipientName, recipientEmailAddress, subject, htmlText);

    }

    @Async
    public void sendAccountConfirmationEmails(String nameEmployee, String emailEmployee, String codeEmployee,
                                              String nameDirector, String emailDirector, String codeDirector) {

        String htmlTextEmployee = "test: " + codeEmployee;
        String htmlTextDirector = "testD: " + codeDirector;
        String subject = "Potwierdzenie utworzenia konta";

        sendEmail(nameEmployee, emailEmployee, subject, htmlTextEmployee);
        sendEmail(nameDirector, emailDirector, subject, htmlTextDirector);
    }

    public void sendPasswordResetEmails(String nameEmployee, String emailEmployee, String codeEmployee,
                                        String nameDirector, String emailDirector, String codeDirector) {

        String htmlTextEmployee = "test: " + codeEmployee;
        String htmlTextDirector = "testD: " + codeDirector;
        String subject = "Resetowanie hasła";

        sendEmail(nameEmployee, emailEmployee, subject, htmlTextEmployee);
        sendEmail(nameDirector, emailDirector, subject, htmlTextDirector);
    }

    public void sendRemovalEmail(String name, String email) {

        String htmlText = "Twoje konto zostało usunięte";
        String subject = "Usunięcie konta";
        sendEmail(name, email, subject, htmlText);
    }

    public void sendPasswordChangeEmail(String name, String email) {

        String htmlText = "Adres e-mail przypisany do Twojego konta został zmieniony";
        String subject = "Zmiana adresu e-mail";
        sendEmail(name, email, subject, htmlText);
    }

    public void sendActivityChangedEmail(String name, String email, boolean active) {

        String htmlText = "Aktywność Twojego konta została zmieniona na " + active;
        String subject = "Zmiana aktywności konta";
        sendEmail(name, email, subject, htmlText);
    }
}
