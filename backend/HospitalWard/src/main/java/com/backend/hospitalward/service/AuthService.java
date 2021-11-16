package com.backend.hospitalward.service;

import com.backend.hospitalward.exception.ErrorKey;
import com.backend.hospitalward.exception.NotFoundException;
import com.backend.hospitalward.repository.AccessLevelRepository;
import com.backend.hospitalward.repository.AccountRepository;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Service
@Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED, timeout = 3)
public class AuthService implements UserDetailsService {

    AccountRepository accountRepository;

    AccessLevelRepository accessLevelRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        return accountRepository.findAccountByLogin(login).orElseThrow(() ->
                new NotFoundException(ErrorKey.ACCOUNT_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public String findAccessLevelByLogin(String login) {
        return accessLevelRepository.findAccessLevelByLogin(login).orElseThrow(() ->
                new NotFoundException(ErrorKey.ACCOUNT_NOT_FOUND)).getName();
    }
}
