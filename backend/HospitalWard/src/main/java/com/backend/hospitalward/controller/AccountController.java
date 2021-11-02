package com.backend.hospitalward.controller;

import com.backend.hospitalward.dto.request.account.AccountCreateRequest;
import com.backend.hospitalward.dto.request.account.ChangePasswordRequest;
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
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.*;

import javax.security.enterprise.credential.Password;
import javax.ws.rs.core.Context;
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

    @CacheEvict(cacheNames = "accounts", allEntries = true)
    @PutMapping(path = "/password")
    public ResponseEntity<?> changePassword(@Context SecurityContext securityContext,
                                            @RequestBody ChangePasswordRequest changePasswordRequest){

        accountService.changePassword(securityContext.getAuthentication().getName(),
                new Password(changePasswordRequest.getOldPassword()), new Password(changePasswordRequest.getNewPassword()));

        return ResponseEntity.ok().build();
    }

    @CacheEvict(cacheNames = "accounts", allEntries = true)
    @PutMapping(path = "/activate/{login}")
    public ResponseEntity<?> activateAccount(@Context SecurityContext securityContext,
                                             @PathVariable("login") String login){

        accountService.changeActivity(login, true, securityContext.getAuthentication().getName());

        return ResponseEntity.ok().build();
    }

    @CacheEvict(cacheNames = "accounts", allEntries = true)
    @PutMapping(path = "/deactivate/{login}")
    public ResponseEntity<?> deactivateAccount(@Context SecurityContext securityContext,
                                             @PathVariable("login") String login){

        accountService.changeActivity(login, false, securityContext.getAuthentication().getName());

        return ResponseEntity.ok().build();
    }

    public void updateAccountOffice(){}

    public void updateAccountMedic(){}

    public void confirmAccount(){}

    public void changeEmailAddress(){}

    public void resetPassword(){}
}
