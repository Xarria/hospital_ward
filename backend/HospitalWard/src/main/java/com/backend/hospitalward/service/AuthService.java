package com.backend.hospitalward.service;


import com.backend.hospitalward.repository.AccessLevelRepository;
import com.backend.hospitalward.repository.AccountRepository;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Service
public class AuthService implements UserDetailsService {

    AccountRepository accountRepository;

    AccessLevelRepository accessLevelRepository;

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        return accountRepository.findAccountByLogin(login).orElseThrow();
    }

    public String findAccessLevelByLogin(String login) {
        return accessLevelRepository.findAccessLevelByLogin(login).orElseThrow().getName();
    }
}
