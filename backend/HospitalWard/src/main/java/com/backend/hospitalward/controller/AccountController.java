package com.backend.hospitalward.controller;

import com.backend.hospitalward.dto.response.AccountGeneralResponse;
import com.backend.hospitalward.mapper.AccountMapper;
import com.backend.hospitalward.service.AccountService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class AccountController {

    AccountService accountService;

    AccountMapper accountMapper;

    @GetMapping("/accounts")
    public ResponseEntity<List<AccountGeneralResponse>> getAllAccounts() {
        return ResponseEntity.ok(accountService.getAllAccounts().stream()
                .map(accountMapper::toAccountGeneralResponse)
                .collect(Collectors.toList()));
    }
}
