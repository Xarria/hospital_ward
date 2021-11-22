package com.backend.hospitalward.controller;

import com.backend.hospitalward.dto.request.account.AccountCreateRequest;
import com.backend.hospitalward.dto.request.account.AccountUpdateRequest;
import com.backend.hospitalward.dto.request.account.ChangePasswordRequest;
import com.backend.hospitalward.dto.request.account.ResetPasswordRequest;
import com.backend.hospitalward.dto.request.medicalStaff.MedicalStaffCreateRequest;
import com.backend.hospitalward.dto.request.medicalStaff.MedicalStaffUpdateRequest;
import com.backend.hospitalward.dto.response.account.AccountDetailsResponse;
import com.backend.hospitalward.dto.response.account.AccountGeneralResponse;
import com.backend.hospitalward.exception.BadRequestException;
import com.backend.hospitalward.exception.ConstraintViolationException;
import com.backend.hospitalward.exception.ErrorKey;
import com.backend.hospitalward.exception.PreconditionFailedException;
import com.backend.hospitalward.mapper.AccountMapper;
import com.backend.hospitalward.model.MedicalStaff;
import com.backend.hospitalward.model.common.AccessLevelName;
import com.backend.hospitalward.security.annotation.*;
import com.backend.hospitalward.service.AccountService;
import com.backend.hospitalward.util.etag.DTOSignatureValidator;
import com.backend.hospitalward.util.etag.ETagValidator;
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

    AccountMapper accountMapper;

    private List<String> getSpecializations(List<String> specializations) {
        return specializations != null ? specializations : Collections.emptyList();
    }

    private void checkETagHeader(@CurrentSecurityContext SecurityContext securityContext
            , @RequestBody @Valid AccountUpdateRequest accountUpdateRequest, @RequestHeader("If-Match") String eTag) {

        if (accountUpdateRequest.getLogin() == null || accountUpdateRequest.getVersion() == null
                || ETagValidator.verifyDTOIntegrity(eTag, accountUpdateRequest)) {
            throw new PreconditionFailedException(ErrorKey.ETAG_INVALID);
        }
        if (!accountUpdateRequest.getLogin().equals(securityContext.getAuthentication().getName())) {
            throw new PreconditionFailedException(ErrorKey.NOT_OWN_ACCOUNT);
        }
    }

    private boolean isValidAccessLevel(String accessLevel) {
        List<String> validAccessLevels = List.of(AccessLevelName.TREATMENT_DIRECTOR, AccessLevelName.HEAD_NURSE,
                AccessLevelName.DOCTOR, AccessLevelName.SECRETARY);
        return accessLevel != null && validAccessLevels.contains(accessLevel);
    }

    //region GET

    @TreatmentDirectorAuthority
    @GetMapping()
    public ResponseEntity<List<AccountGeneralResponse>> getAllAccounts() {
        return ResponseEntity.ok(accountService.getAllAccounts().stream()
                .map(accountMapper::toAccountGeneralResponse)
                .collect(Collectors.toList()));
    }

    @TreatmentDirectorAuthority
    @GetMapping("/{login}")
    public ResponseEntity<AccountDetailsResponse> getAccountByLogin(@PathVariable("login") String login) {
        AccountDetailsResponse account = accountMapper.toAccountDetailsResponse(accountService.getAccountByLogin(login));

        return ResponseEntity.ok()
                .eTag(ETagValidator.calculateDTOSignature(account))
                .body(account);
    }

    @Authenticated
    @GetMapping(path = "/profile")
    public ResponseEntity<AccountGeneralResponse> getProfile(@CurrentSecurityContext SecurityContext securityContext) {
        AccountGeneralResponse account = accountMapper.toAccountGeneralResponse(
                accountService.getAccountByLogin(securityContext.getAuthentication().getName()));

        return ResponseEntity.ok()
                .eTag(ETagValidator.calculateDTOSignature(account))
                .body(account);
    }

    //endregion

    //region CREATE

    @TreatmentDirectorAuthority
    @PostMapping(path = "/office")
    public ResponseEntity<?> createAccountOffice(@CurrentSecurityContext SecurityContext securityContext,
                                                 @RequestBody @Valid AccountCreateRequest accountCreateRequest) {
        accountService.createAccount(accountMapper.toAccount(accountCreateRequest), accountCreateRequest.getAccessLevel(),
                securityContext.getAuthentication().getName());
        return ResponseEntity.ok().build();
    }

    @TreatmentDirectorAuthority
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

    @Authenticated
    @PutMapping(path = "/password")
    public ResponseEntity<?> changePassword(@CurrentSecurityContext SecurityContext securityContext,
                                            @RequestBody @Valid ChangePasswordRequest changePasswordRequest) {

        accountService.changePassword(securityContext.getAuthentication().getName(),
                new Password(changePasswordRequest.getOldPassword()), new Password(changePasswordRequest.getNewPassword()));

        return ResponseEntity.ok().build();
    }

    @TreatmentDirectorAuthority
    @PutMapping(path = "/activate/{login}")
    public ResponseEntity<?> activateAccount(@CurrentSecurityContext SecurityContext securityContext,
                                             @PathVariable("login") String login) {

        accountService.changeActivity(login, true, securityContext.getAuthentication().getName());

        return ResponseEntity.ok().build();
    }

    @TreatmentDirectorAuthority
    @PutMapping(path = "/deactivate/{login}")
    public ResponseEntity<?> deactivateAccount(@CurrentSecurityContext SecurityContext securityContext,
                                               @PathVariable("login") String login) {

        accountService.changeActivity(login, false, securityContext.getAuthentication().getName());

        return ResponseEntity.ok().build();
    }

    @TreatmentDirectorAuthority
    @DTOSignatureValidator
    @PutMapping(path = "/office/edit/{login}", headers = "If-Match")
    public ResponseEntity<?> updateAccountOffice(@CurrentSecurityContext SecurityContext securityContext,
                                                 @RequestBody @Valid AccountUpdateRequest accountUpdateRequest,
                                                 @RequestHeader("If-Match") String eTag) {

        if (accountUpdateRequest.getLogin() == null || accountUpdateRequest.getVersion() == null
                || ETagValidator.verifyDTOIntegrity(eTag, accountUpdateRequest)) {
            throw new PreconditionFailedException(ErrorKey.ETAG_INVALID);
        }

        accountService.updateAccountOffice(accountMapper.toAccount(accountUpdateRequest),
                securityContext.getAuthentication().getName());

        return ResponseEntity.ok().build();
    }

    @TreatmentDirectorAuthority
    @DTOSignatureValidator
    @PutMapping(path = "/medic/edit/{login}", headers = "If-Match")
    public ResponseEntity<?> updateAccountMedic(@CurrentSecurityContext SecurityContext securityContext,
                                                @RequestBody @Valid MedicalStaffUpdateRequest medicalStaffUpdateRequest,
                                                @RequestHeader("If-Match") String eTag) {

        if (medicalStaffUpdateRequest.getLogin() == null || medicalStaffUpdateRequest.getVersion() == null
                || ETagValidator.verifyDTOIntegrity(eTag, medicalStaffUpdateRequest)) {
            throw new PreconditionFailedException(ErrorKey.ETAG_INVALID);
        }

        accountService.updateMedicalStaff((MedicalStaff) accountMapper.toAccount(medicalStaffUpdateRequest),
                securityContext.getAuthentication().getName(), getSpecializations(medicalStaffUpdateRequest.getSpecializations()));

        return ResponseEntity.ok().build();
    }

    @OfficeAuthorities
    @DTOSignatureValidator
    @PutMapping(path = "/profile/office/edit", headers = "If-Match")
    public ResponseEntity<?> updateOwnAccountOffice(@CurrentSecurityContext SecurityContext securityContext,
                                                    @RequestBody @Valid AccountUpdateRequest accountUpdateRequest,
                                                    @RequestHeader("If-Match") String eTag) {

        checkETagHeader(securityContext, accountUpdateRequest, eTag);

        accountService.updateAccountOffice(accountMapper.toAccount(accountUpdateRequest), accountUpdateRequest.getLogin());

        return ResponseEntity.ok().build();
    }

    @MedicAuthorities
    @DTOSignatureValidator
    @PutMapping(path = "/profile/medic/edit", headers = "If-Match")
    public ResponseEntity<?> updateOwnAccountMedic(@CurrentSecurityContext SecurityContext securityContext,
                                                   @RequestBody @Valid MedicalStaffUpdateRequest medicalStaffUpdateRequest,
                                                   @RequestHeader("If-Match") String eTag) {

        checkETagHeader(securityContext, medicalStaffUpdateRequest, eTag);

        accountService.updateMedicalStaff((MedicalStaff) accountMapper.toAccount(medicalStaffUpdateRequest),
                medicalStaffUpdateRequest.getLogin(), getSpecializations(medicalStaffUpdateRequest.getSpecializations()));

        return ResponseEntity.ok().build();
    }

    @TreatmentDirectorAuthority
    @PutMapping(path = "/accessLevel/{login}")
    public ResponseEntity<?> changeAccessLevel(@CurrentSecurityContext SecurityContext securityContext,
                                               @RequestBody String newAccessLevel, @PathVariable("login") String login) {

        if (!isValidAccessLevel(newAccessLevel) || login == null || login.length() == 0) {
            throw new BadRequestException(ErrorKey.INVALID_LEVEL_OR_LOGIN);
        }
        accountService.changeAccessLevel(newAccessLevel, login, securityContext.getAuthentication().getName());

        return ResponseEntity.ok().build();
    }

    @PermitAll
    @PutMapping(path = "/confirm/{url}")
    public ResponseEntity<?> confirmAccount(@PathVariable("url") String url, @RequestBody String password) {

        if (url.length() != 10) {
            throw new ConstraintViolationException(ErrorKey.URL_INVALID);
        }

        if (password == null || password.length() < 8) {
            throw new ConstraintViolationException(ErrorKey.PASSWORD_INCORRECT);
        }

        accountService.confirmAccount(url, new Password(password));

        return ResponseEntity.ok().build();
    }

    @Authenticated
    @PutMapping(path = "/edit/email")
    public ResponseEntity<?> changeOwnEmailAddress(@CurrentSecurityContext SecurityContext securityContext,
                                                   @RequestBody String newEmail) {

        if (!EmailAddressValidator.isValid(newEmail)) {
            throw new ConstraintViolationException(ErrorKey.EMAIL_INVALID);
        }

        accountService.changeEmailAddress(newEmail, securityContext.getAuthentication().getName(),
                securityContext.getAuthentication().getName());

        return ResponseEntity.ok().build();
    }

    @TreatmentDirectorAuthority
    @PutMapping(path = "/edit/email/{login}")
    public ResponseEntity<?> changeEmailAddress(@CurrentSecurityContext SecurityContext securityContext,
                                                @PathVariable("login") String login, @RequestBody String newEmail) {

        if (!EmailAddressValidator.isValid(newEmail)) {
            throw new ConstraintViolationException(ErrorKey.EMAIL_INVALID);
        }

        accountService.changeEmailAddress(newEmail, login, securityContext.getAuthentication().getName());

        return ResponseEntity.ok().build();
    }

    @PermitAll
    @PutMapping(path = "/password/reset/{url}")
    public ResponseEntity<?> resetPassword(@PathVariable("url") String url, @RequestBody String newPassword) {

        if (newPassword == null || newPassword.length() < 8) {
            throw new ConstraintViolationException(ErrorKey.PASSWORD_INCORRECT);
        }
        if (url == null || url.length() != 10) {
            throw new ConstraintViolationException(ErrorKey.URL_INVALID);
        }

        accountService.resetPassword(url, new Password(newPassword));

        return ResponseEntity.ok().build();
    }

    //endregion

    //region DELETE

    @TreatmentDirectorAuthority
    @DeleteMapping(path = "/{login}")
    public ResponseEntity<?> deleteUnconfirmedAccount(@PathVariable("login") String login) {

        accountService.deleteUnconfirmedAccount(login);

        return ResponseEntity.ok().build();
    }
    //endregion

    //region EMAILS

    @PermitAll
    @PostMapping(path = "/password/reset")
    public ResponseEntity<?> sendResetPasswordUrl(@CurrentSecurityContext SecurityContext securityContext,
                                                  @RequestBody @Valid ResetPasswordRequest resetPasswordRequest) {

        accountService.sendResetPasswordUrl(resetPasswordRequest.getEmail(), resetPasswordRequest.getNameDirector(),
                resetPasswordRequest.getSurnameDirector(), securityContext.getAuthentication().getName());

        return ResponseEntity.ok().build();
    }

    //endregion
}
