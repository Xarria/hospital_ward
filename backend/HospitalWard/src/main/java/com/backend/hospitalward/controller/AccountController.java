package com.backend.hospitalward.controller;

import com.backend.hospitalward.dto.request.account.AccountCreateRequest;
import com.backend.hospitalward.dto.request.medicalStaff.MedicalStaffCreateRequest;
import com.backend.hospitalward.dto.response.account.AccountDetailsResponse;
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
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    AccountService accountService;

    AccountMapper accountMapper;

    @Cacheable("accounts")
    @GetMapping()
    public ResponseEntity<List<AccountGeneralResponse>> getAllAccounts() {
        return ResponseEntity.ok(accountService.getAllAccounts().stream()
                .map(accountMapper::toAccountGeneralResponse)
                .collect(Collectors.toList()));
    }

    @Cacheable("accounts")
    @GetMapping("/{login}")
    public ResponseEntity<AccountDetailsResponse> getAccountByLogin(@PathVariable("login") String login) {
        return ResponseEntity.ok(accountMapper.toAccountDetailsResponse(accountService.getAccountByLogin(login)));
    }

    @CacheEvict(cacheNames = "accounts", allEntries = true)
    @PostMapping(path = "/office")
    public ResponseEntity<?> createAccountOffice(@RequestBody AccountCreateRequest accountCreateRequest) {
        accountService.createAccount(accountMapper.toAccount(accountCreateRequest), accountCreateRequest.getAccessLevel());
        return ResponseEntity.ok().build();
    }

    @CacheEvict(cacheNames = "accounts", allEntries = true)
    @PostMapping(path = "/medic")
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
