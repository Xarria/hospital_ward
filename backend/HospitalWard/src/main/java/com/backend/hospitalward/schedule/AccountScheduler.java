package com.backend.hospitalward.schedule;

import com.backend.hospitalward.model.Account;
import com.backend.hospitalward.model.Url;
import com.backend.hospitalward.model.common.UrlActionType;
import com.backend.hospitalward.repository.AccountRepository;
import com.backend.hospitalward.repository.UrlRepository;
import com.backend.hospitalward.util.notification.EmailSender;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class AccountScheduler {

    final AccountRepository accountRepository;

    final UrlRepository urlRepository;

    final EmailSender emailSender;

    @Scheduled(cron = "0 30 0 * * *")
    public void removeUnconfirmedAccounts() {
        long removalTime = 86400L;

        List<Account> accountsToDelete = accountRepository.findAccountsByUnconfirmedAndExpired(removalTime);
        List<List<Url>> urlsToDelete = new ArrayList<>();
        accountsToDelete.forEach(
                account -> urlsToDelete.add(urlRepository.findUrlsByAccountEmployee(account)));

        urlsToDelete.stream()
                .flatMap(List::stream)
                .forEach(urlRepository::delete);
        accountRepository.deleteAll(accountsToDelete);

        accountsToDelete.forEach(account -> emailSender.sendRemovalEmail(account.getName(), account.getEmail()));
    }

    @Scheduled(cron = "0 0 * * * *")
    public void removeExpiredUrl() {
        List<Url> expired = urlRepository.findUrlsByExpirationDateBefore(Timestamp.from(Instant.now()));

        urlRepository.deleteAll(expired);
    }

}
