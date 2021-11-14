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
import com.backend.hospitalward.util.notification.EmailSender;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.hazlewood.connor.bottema.emailaddress.EmailAddressValidator;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.*;

import javax.security.enterprise.credential.Password;
import javax.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    AccountService accountService;

    EmailSender emailSender;

    AccountMapper accountMapper;

    private List<String> getSpecializations(List<String> specializations) {
        return specializations != null ? specializations : Collections.emptyList();
    }

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
    public ResponseEntity<AccountGeneralDTO> getProfile(@CurrentSecurityContext SecurityContext securityContext) {
        AccountGeneralDTO account = accountMapper.toAccountGeneralResponse(
                accountService.getAccountByLogin(securityContext.getAuthentication().getName()));

        return ResponseEntity.ok()
                .eTag(ETagValidator.calculateDTOSignature(account))
                .body(account);
    }

    //endregion

    //region CREATE

    @PostMapping(path = "/office")
    public ResponseEntity<?> createAccountOffice(@CurrentSecurityContext SecurityContext securityContext,
                                                 @RequestBody @Valid AccountCreateRequest accountCreateRequest) {
        accountService.createAccount(accountMapper.toAccount(accountCreateRequest), accountCreateRequest.getAccessLevel(),
                securityContext.getAuthentication().getName());
        return ResponseEntity.ok().build();
    }

    @PostMapping(path = "/medic")
    public ResponseEntity<?> createAccountMedic(@CurrentSecurityContext SecurityContext securityContext,
                                                @RequestBody @Valid MedicalStaffCreateRequest medicalStaffCreateRequest) {
        accountService.createMedicalStaff((MedicalStaff) accountMapper.toAccount(medicalStaffCreateRequest),
                medicalStaffCreateRequest.getAccessLevel(), getSpecializations(medicalStaffCreateRequest.getSpecializations()),
                securityContext.getAuthentication().getName());

        return ResponseEntity.ok().build();
    }

    //endregion

    //region UPDATES

    @PutMapping(path = "/password")
    public ResponseEntity<?> changePassword(@CurrentSecurityContext SecurityContext securityContext,
                                            @RequestBody ChangePasswordRequest changePasswordRequest) {

        accountService.changePassword(securityContext.getAuthentication().getName(),
                new Password(changePasswordRequest.getOldPassword()), new Password(changePasswordRequest.getNewPassword()));

        return ResponseEntity.ok().build();
    }

    @PutMapping(path = "/activate/{login}")
    public ResponseEntity<?> activateAccount(@CurrentSecurityContext SecurityContext securityContext,
                                             @PathVariable("login") String login) {

        accountService.changeActivity(login, true, securityContext.getAuthentication().getName());

        return ResponseEntity.ok().build();
    }

    @PutMapping(path = "/deactivate/{login}")
    public ResponseEntity<?> deactivateAccount(@CurrentSecurityContext SecurityContext securityContext,
                                               @PathVariable("login") String login) {

        accountService.changeActivity(login, false, securityContext.getAuthentication().getName());

        return ResponseEntity.ok().build();
    }

    @DTOSignatureValidator
    @PutMapping(path = "/office/edit/{login}", headers = "If-Match")
    public ResponseEntity<?> updateAccountOffice(@CurrentSecurityContext SecurityContext securityContext,
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
    public ResponseEntity<?> updateAccountMedic(@CurrentSecurityContext SecurityContext securityContext,
                                                @RequestBody @Valid MedicalStaffUpdateRequest medicalStaffUpdateRequest,
                                                @RequestHeader("If-Match") String eTag) {

        if (medicalStaffUpdateRequest.getLogin() == null || medicalStaffUpdateRequest.getVersion() == null
                || ETagValidator.verifyDTOIntegrity(eTag, medicalStaffUpdateRequest)) {
            throw CommonException.createPreconditionFailedException();
        }

        accountService.updateMedicalStaff((MedicalStaff) accountMapper.toAccount(medicalStaffUpdateRequest),
                securityContext.getAuthentication().getName(), getSpecializations(medicalStaffUpdateRequest.getSpecializations()));

        return ResponseEntity.ok().build();
    }

    @DTOSignatureValidator
    @PutMapping(path = "/profile/office/edit", headers = "If-Match")
    public ResponseEntity<?> updateOwnAccountOffice(@CurrentSecurityContext SecurityContext securityContext,
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
    public ResponseEntity<?> updateOwnAccountMedic(@CurrentSecurityContext SecurityContext securityContext,
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
                medicalStaffUpdateRequest.getLogin(), getSpecializations(medicalStaffUpdateRequest.getSpecializations()));

        return ResponseEntity.ok().build();
    }

    @PutMapping(path = "/accessLevel/{login}")
    public ResponseEntity<?> changeAccessLevel(@CurrentSecurityContext SecurityContext securityContext,
                                               @RequestBody String newAccessLevel, @PathVariable("login") String login) {

        if (newAccessLevel == null || newAccessLevel.length() == 0 || login == null || login.length() == 0) {
            throw CommonException.createConstraintViolationException();
        }
        accountService.changeAccessLevel(newAccessLevel, login, securityContext.getAuthentication().getName());

        return ResponseEntity.ok().build();
    }

    @PutMapping(path = "/confirm/{url}")
    public ResponseEntity<?> confirmAccount(@PathVariable("url") String url) {

        if (url.length() != 10) {
            throw CommonException.createConstraintViolationException();
        }

        accountService.confirmAccount(url);

        return ResponseEntity.ok().build();
    }

    @PutMapping(path = "/edit/email")
    public ResponseEntity<?> changeEmailAddress(@CurrentSecurityContext SecurityContext securityContext,
                                                @RequestBody String newEmail) {
        if (!EmailAddressValidator.isValid(newEmail)) {
            throw CommonException.createConstraintViolationException();
        }

        accountService.changeEmailAddress(newEmail, securityContext.getAuthentication().getName());

        return ResponseEntity.ok().build();
    }

    @GetMapping(path = "/password/reset/{url}")
    public ResponseEntity<?> resetPassword(@PathVariable("url") String url) {

        return ResponseEntity.ok().build();
    }

    //endregion

    //region EMAILS

    @PostMapping(path = "/password/reset")
    public ResponseEntity<?> sendResetPasswordUrl(@RequestBody String email) {

        emailSender.sendResetPasswordUrl(email);

        return ResponseEntity.ok().build();
    }


    //endregion
}
