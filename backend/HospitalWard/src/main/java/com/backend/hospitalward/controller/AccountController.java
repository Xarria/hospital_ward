package com.backend.hospitalward.controller;

import com.backend.hospitalward.dto.request.account.AccountCreateRequest;
import com.backend.hospitalward.dto.request.account.AccountUpdateRequest;
import com.backend.hospitalward.dto.request.account.ChangePasswordRequest;
import com.backend.hospitalward.dto.request.medicalStaff.MedicalStaffCreateRequest;
import com.backend.hospitalward.dto.request.medicalStaff.MedicalStaffUpdateRequest;
import com.backend.hospitalward.dto.response.account.AccountDetailsDTO;
import com.backend.hospitalward.dto.response.account.AccountGeneralDTO;
import com.backend.hospitalward.exception.CommonException;
import com.backend.hospitalward.mapper.AccountMapper;
import com.backend.hospitalward.model.MedicalStaff;
import com.backend.hospitalward.service.AccountService;
import com.backend.hospitalward.util.etag.DTOSignatureValidator;
import com.backend.hospitalward.util.etag.ETagValidator;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.*;

import javax.security.enterprise.credential.Password;
import javax.validation.Valid;
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

    //region GET

    @GetMapping()
    public ResponseEntity<List<AccountGeneralDTO>> getAllAccounts() {
        return ResponseEntity.ok(accountService.getAllAccounts().stream()
                .map(accountMapper::toAccountGeneralResponse)
                .collect(Collectors.toList()));
    }

    @GetMapping("/{login}")
    public ResponseEntity<AccountDetailsDTO> getAccountByLogin(@PathVariable("login") String login) {
        AccountDetailsDTO account = accountMapper.toAccountDetailsResponse(accountService.getAccountByLogin(login));

        return ResponseEntity.ok()
                .eTag(ETagValidator.calculateDTOSignature(account))
                .body(account);
    }

    @GetMapping(path = "/profile")
    public ResponseEntity<AccountGeneralDTO> getProfile(@Context SecurityContext securityContext) {
        AccountGeneralDTO account = accountMapper.toAccountGeneralResponse(
                accountService.getAccountByLogin(securityContext.getAuthentication().getName()));

        return ResponseEntity.ok()
                .eTag(ETagValidator.calculateDTOSignature(account))
                .body(account);
    }

    //endregion

    //region CREATE

    @PostMapping(path = "/office")
    public ResponseEntity<?> createAccountOffice(@Context SecurityContext securityContext,
                                                 @RequestBody @Valid AccountCreateRequest accountCreateRequest) {
        accountService.createAccount(accountMapper.toAccount(accountCreateRequest), accountCreateRequest.getAccessLevel(),
                securityContext.getAuthentication().getName());
        return ResponseEntity.ok().build();
    }

    @PostMapping(path = "/medic")
    public ResponseEntity<?> createAccountMedic(@Context SecurityContext securityContext,
                                                @RequestBody @Valid MedicalStaffCreateRequest medicalStaffCreateRequest) {
        accountService.createMedicalStaff((MedicalStaff) accountMapper.toAccount(medicalStaffCreateRequest),
                medicalStaffCreateRequest.getAccessLevel(), medicalStaffCreateRequest.getSpecializations(),
                securityContext.getAuthentication().getName());

        return ResponseEntity.ok().build();
    }

    //endregion

    //region UPDATES

    @PutMapping(path = "/password")
    public ResponseEntity<?> changePassword(@Context SecurityContext securityContext,
                                            @RequestBody ChangePasswordRequest changePasswordRequest) {

        accountService.changePassword(securityContext.getAuthentication().getName(),
                new Password(changePasswordRequest.getOldPassword()), new Password(changePasswordRequest.getNewPassword()));

        return ResponseEntity.ok().build();
    }

    @PutMapping(path = "/activate/{login}")
    public ResponseEntity<?> activateAccount(@Context SecurityContext securityContext,
                                             @PathVariable("login") String login) {

        accountService.changeActivity(login, true, securityContext.getAuthentication().getName());

        return ResponseEntity.ok().build();
    }

    @PutMapping(path = "/deactivate/{login}")
    public ResponseEntity<?> deactivateAccount(@Context SecurityContext securityContext,
                                               @PathVariable("login") String login) {

        accountService.changeActivity(login, false, securityContext.getAuthentication().getName());

        return ResponseEntity.ok().build();
    }

    @DTOSignatureValidator
    @PutMapping(path = "/office/edit/{login}", headers = "If-Match")
    public ResponseEntity<?> updateAccountOffice(@Context SecurityContext securityContext,
                                                 @RequestBody @Valid AccountUpdateRequest accountUpdateRequest,
                                                 @RequestHeader("If-Match") String eTag) {

        if (accountUpdateRequest.getLogin() == null || accountUpdateRequest.getVersion() == null
                || ETagValidator.verifyDTOIntegrity(eTag, accountUpdateRequest)) {
            throw CommonException.createPreconditionFailedException();
        }

        accountService.updateAccount(accountMapper.toAccount(accountUpdateRequest),
                securityContext.getAuthentication().getName());

        return ResponseEntity.ok().build();
    }

    @DTOSignatureValidator
    @PutMapping(path = "/medic/edit/{login}", headers = "If-Match")
    public ResponseEntity<?> updateAccountMedic(@Context SecurityContext securityContext,
                                                @RequestBody @Valid MedicalStaffUpdateRequest medicalStaffUpdateRequest,
                                                @RequestHeader("If-Match") String eTag) {

        if (medicalStaffUpdateRequest.getLogin() == null || medicalStaffUpdateRequest.getVersion() == null
                || ETagValidator.verifyDTOIntegrity(eTag, medicalStaffUpdateRequest)) {
            throw CommonException.createPreconditionFailedException();
        }

        accountService.updateMedicalStaff((MedicalStaff) accountMapper.toAccount(medicalStaffUpdateRequest),
                securityContext.getAuthentication().getName(), medicalStaffUpdateRequest.getSpecializations());

        return ResponseEntity.ok().build();
    }

    @DTOSignatureValidator
    @PutMapping(path = "/profile/office/edit", headers = "If-Match")
    public ResponseEntity<?> updateOwnAccountOffice(@Context SecurityContext securityContext,
                                                    @RequestBody @Valid AccountUpdateRequest accountUpdateRequest,
                                                    @RequestHeader("If-Match") String eTag) {

        if (accountUpdateRequest.getLogin() == null || accountUpdateRequest.getVersion() == null
                || ETagValidator.verifyDTOIntegrity(eTag, accountUpdateRequest)) {
            throw CommonException.createPreconditionFailedException();
        }
        if (!accountUpdateRequest.getLogin().equals(securityContext.getAuthentication().getName())) {
            throw CommonException.createPreconditionFailedException();
        }

        accountService.updateAccount(accountMapper.toAccount(accountUpdateRequest), accountUpdateRequest.getLogin());

        return ResponseEntity.ok().build();
    }

    @DTOSignatureValidator
    @PutMapping(path = "/profile/medic/edit", headers = "If-Match")
    public ResponseEntity<?> updateOwnAccountMedic(@Context SecurityContext securityContext,
                                                   @RequestBody @Valid MedicalStaffUpdateRequest medicalStaffUpdateRequest,
                                                   @RequestHeader("If-Match") String eTag) {

        if (medicalStaffUpdateRequest.getLogin() == null || medicalStaffUpdateRequest.getVersion() == null
                || ETagValidator.verifyDTOIntegrity(eTag, medicalStaffUpdateRequest)) {
            throw CommonException.createPreconditionFailedException();
        }
        if (!medicalStaffUpdateRequest.getLogin().equals(securityContext.getAuthentication().getName())) {
            throw CommonException.createPreconditionFailedException();
        }

        accountService.updateMedicalStaff((MedicalStaff) accountMapper.toAccount(medicalStaffUpdateRequest),
                medicalStaffUpdateRequest.getLogin(), medicalStaffUpdateRequest.getSpecializations());

        return ResponseEntity.ok().build();
    }

    public void changeAccessLevel() {
    }

    @PutMapping(path = "/confirm/{url}")
    public ResponseEntity<?> confirmAccount(@PathVariable("url") String url) {

        accountService.confirmAccount(url);

        return ResponseEntity.ok().build();
    }

    public void changeEmailAddress() {
    }

    public void resetPassword() {
    }

    //endregion

    //region EMAILS

    public void sendResetPasswordUrl() {
    }

    public void sendChangeEmailUrl() {
    }

    public void sendChangeOwnEmailUrl() {
    }

    //endregion
}
