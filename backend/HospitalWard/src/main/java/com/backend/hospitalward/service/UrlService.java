package com.backend.hospitalward.service;

import com.backend.hospitalward.exception.BadRequestException;
import com.backend.hospitalward.exception.ErrorKey;
import com.backend.hospitalward.exception.GoneException;
import com.backend.hospitalward.exception.NotFoundException;
import com.backend.hospitalward.model.Account;
import com.backend.hospitalward.model.Url;
import com.backend.hospitalward.model.common.UrlActionType;
import com.backend.hospitalward.repository.UrlRepository;
import com.backend.hospitalward.util.notification.EmailSender;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.RandomStringUtils;
import org.hibernate.HibernateException;
import org.hibernate.JDBCException;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.PersistenceException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.SECONDS;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@Retryable(value = {PersistenceException.class, HibernateException.class, JDBCException.class},
        exclude = ConstraintViolationException.class, backoff = @Backoff(delay = 1000))
@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, timeout = 3)
public class UrlService {

    UrlRepository urlRepository;

    EmailSender emailSender;

    public void deleteUrl(Url url) {
        urlRepository.delete(url);
    }

    public void deleteUrlsForAccount(Account account) {
        List<Url> urlsForAccount = urlRepository.findUrlsByAccountEmployee(account);

        urlRepository.deleteAll(urlsForAccount);
    }

    public void deleteOldConfirmationUrls(Account account) {
        List<Url> urls = urlRepository.findUrlsByAccountEmployee(account).stream()
                .filter(url -> url.getActionType().equals(UrlActionType.CONFIRM.name()))
                .collect(Collectors.toList());

        urlRepository.deleteAll(urls);
    }

    public void createConfirmUrl(Account account, Account director) {
        Url url = Url.builder()
                .codeDirector(RandomStringUtils.randomAlphanumeric(5))
                .codeEmployee(RandomStringUtils.randomAlphanumeric(5))
                .accountDirector(director)
                .accountEmployee(account)
                .actionType(UrlActionType.CONFIRM.name())
                .creationDate(Timestamp.from(Instant.now()))
                .expirationDate(Timestamp.from(Instant.now().plus(86400, SECONDS)))
                .createdBy(director)
                .build();

        urlRepository.save(url);

        emailSender.sendAccountConfirmationEmails(account.getName(), account.getEmail(), url.getCodeEmployee(),
                director.getName(), director.getEmail(), url.getCodeDirector());
    }

    public Url createResetPasswordUrl(Account accountEmployee, Account accountDirector) {
        return Url.builder()
                .creationDate(Timestamp.from(Instant.now()))
                .accountEmployee(accountEmployee)
                .accountDirector(accountDirector)
                .codeEmployee(RandomStringUtils.randomAlphanumeric(5))
                .codeDirector(RandomStringUtils.randomAlphanumeric(5))
                .actionType(UrlActionType.PASSWORD.name())
                .creationDate(Timestamp.from(Instant.now()))
                .expirationDate(Timestamp.from(Instant.now().plus(86400, SECONDS)))
                .build();
    }

    public void deleteOldAndCreateResetPasswordUrl(boolean setRequestedBy, Account accountEmployee,
                                                   Account accountDirector, String email) {
        List<Url> urls = urlRepository.findUrlsByAccountEmployee(accountEmployee).stream()
                .filter(url -> url.getActionType().equals(UrlActionType.PASSWORD.name()))
                .collect(Collectors.toList());

        urlRepository.deleteAll(urls);

        Url url = createResetPasswordUrl(accountEmployee, accountDirector);

        if (setRequestedBy) {
            url.setCreatedBy(accountDirector);
        }
        urlRepository.save(url);

        emailSender.sendPasswordResetEmails(accountEmployee.getName(), email, url.getCodeEmployee(),
                accountDirector.getName(), accountDirector.getEmail(), url.getCodeDirector());
    }

    public Url validateUrl(String urlCode, String actionType) {
        if (urlCode == null || urlCode.length() != 10) {
            throw new NotFoundException(ErrorKey.URL_NOT_FOUND);
        }

        Url url = urlRepository.findUrlByCodeDirectorAndCodeEmployee(urlCode.substring(0, 5), urlCode.substring(5, 10))
                .orElseThrow(() -> new NotFoundException(ErrorKey.URL_NOT_FOUND));

        if (Instant.now().isAfter(url.getExpirationDate().toInstant())) {
            throw new GoneException(ErrorKey.URL_EXPIRED);
        } else if (!url.getActionType().equals(actionType)) {
            throw new BadRequestException(ErrorKey.URL_WRONG_ACTION);
        }

        if (!urlCode.equals(url.getCodeDirector() + url.getCodeEmployee())) {
            throw new NotFoundException(ErrorKey.URL_NOT_FOUND);
        }
        return url;
    }

}
