package com.backend.hospitalward.controller;

import com.backend.hospitalward.dto.request.account.AccountCreateRequest;
import com.backend.hospitalward.dto.request.medicalStaff.MedicalStaffCreateRequest;
import com.backend.hospitalward.dto.response.account.AccountGeneralResponse;
import com.backend.hospitalward.mapper.AccountMapper;
import com.backend.hospitalward.model.MedicalStaff;
import com.backend.hospitalward.service.AccountService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class AccountController {

    AccountService accountService;

    AccountMapper accountMapper;

    @Cacheable("accounts")
    @GetMapping("/accounts")
    public ResponseEntity<List<AccountGeneralResponse>> getAllAccounts() {
        return ResponseEntity.ok(accountService.getAllAccounts().stream()
                .map(accountMapper::toAccountGeneralResponse)
                .collect(Collectors.toList()));
    }

    @CacheEvict(cacheNames = "accounts", allEntries = true)
    @PostMapping(path = "/accounts/office")
    public ResponseEntity<?> createAccountOffice(@RequestBody AccountCreateRequest accountCreateRequest) {
        accountService.createAccount(accountMapper.toAccount(accountCreateRequest), accountCreateRequest.getAccessLevel());
        return ResponseEntity.ok().build();
    }

    @CacheEvict(cacheNames = "accounts", allEntries = true)
    @PostMapping(path = "/accounts/medic")
    public ResponseEntity<?> createAccountMedic(@RequestBody MedicalStaffCreateRequest medicalStaffCreateRequest) {
        accountService.createMedicalStaff((MedicalStaff) accountMapper.toAccount(medicalStaffCreateRequest),
                medicalStaffCreateRequest.getAccessLevel(), medicalStaffCreateRequest.getSpecializations());

        return ResponseEntity.ok().build();
    }

    public void changePassword(){}

    public void updateAccountOffice(){}

    public void updateAccountMedic(){}

    public void activateAccount(){}

    public void deactivateAccount(){}

    public void confirmAccount(){}
}
