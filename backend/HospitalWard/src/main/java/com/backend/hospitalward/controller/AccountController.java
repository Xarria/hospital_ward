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

    @GetMapping("/accounts")
    public ResponseEntity<List<AccountGeneralResponse>> getAllAccounts() {
        return ResponseEntity.ok(accountService.getAllAccounts().stream()
                .map(accountMapper::toAccountGeneralResponse)
                .collect(Collectors.toList()));
    }

    @PostMapping(path = "/accounts")
    public ResponseEntity<?> createAccount(@RequestBody AccountCreateRequest accountCreateRequest) {
        if (accountCreateRequest instanceof MedicalStaffCreateRequest) {
            accountService.createMedicalStaff((MedicalStaff) accountMapper.toAccount(accountCreateRequest),
                    accountCreateRequest.getAccessLevel(), ((MedicalStaffCreateRequest) accountCreateRequest).getSpecializations());

            return ResponseEntity.ok().build();
        }
        accountService.createAccount(accountMapper.toAccount(accountCreateRequest), accountCreateRequest.getAccessLevel());
        return ResponseEntity.ok().build();
    }
}
