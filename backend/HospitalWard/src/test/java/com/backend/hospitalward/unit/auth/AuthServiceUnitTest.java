package com.backend.hospitalward.unit.auth;

import com.backend.hospitalward.model.AccessLevel;
import com.backend.hospitalward.model.Account;
import com.backend.hospitalward.model.common.AccessLevelName;
import com.backend.hospitalward.repository.AccessLevelRepository;
import com.backend.hospitalward.repository.AccountRepository;
import com.backend.hospitalward.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class AuthServiceUnitTest {

    @Mock
    AccountRepository accountRepository;

    @Mock
    AccessLevelRepository accessLevelRepository;

    @InjectMocks
    AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void loadUserByUsername() {
        when(accountRepository.findAccountByLogin(anyString()))
                .thenReturn(Optional.of(Account.builder().login("login").password("b109f3bbbc244eb82441917ed06d618b900" +
                        "8dd09b3befd1b5e07394c706a8bb980b1d7785e5976ec049b46df5f1326af5a2ea6d103fd07c95385ffab0cacbc86")
                        .build()));

        UserDetails userDetails = authService.loadUserByUsername("login");

        assertAll(
                () -> assertNotNull(userDetails),
                () -> assertEquals("login", userDetails.getUsername()),
                () -> assertEquals("b109f3bbbc244eb82441917ed06d618b9008dd09b3befd1b5e07394c706a8bb980b1d7785e" +
                        "5976ec049b46df5f1326af5a2ea6d103fd07c95385ffab0cacbc86", userDetails.getPassword())
        );
    }

    @Test
    void findAccessLevelByLogin() {
        when(accessLevelRepository.findAccessLevelByLogin(anyString()))
                .thenReturn(Optional.of(AccessLevel.builder().name(AccessLevelName.SECRETARY).build()));

        String accessLevel = authService.findAccessLevelByLogin("login");

        assertEquals(AccessLevelName.SECRETARY, accessLevel);
    }
}
