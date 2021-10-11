package com.backend.hospitalward.service;


import com.backend.hospitalward.model.Account;
import com.backend.hospitalward.model.MedicalStaff;
import com.backend.hospitalward.model.Specialization;
import com.backend.hospitalward.repository.AccessLevelRepository;
import com.backend.hospitalward.repository.AccountRepository;
import com.backend.hospitalward.repository.SpecializationRepository;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.token.Sha512DigestUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Service
public class AccountService {

    AccountRepository accountRepository;

    AccessLevelRepository accessLevelRepository;

    SpecializationRepository specializationRepository;

    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    public Account getAccountByLogin(String login) {
        return accountRepository.findAccountByLogin(login).get();
    }

    private void createBaseAccount(Account account, String accessLevel) {
        account.setVersion(0L);
        account.setPassword(Sha512DigestUtils.shaHex(account.getPassword()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUser = authentication.getName();

        account.setCreatedBy(accountRepository.findAccountByLogin(currentUser).get());

        String fullName = (account.getName() + "." + account.getSurname()).toLowerCase();
        int sameNameCount = accountRepository.findAccountsByLoginContains(fullName).size();

        if(sameNameCount == 0) {
            account.setLogin(fullName);
        } else {
            int addedNumber = sameNameCount + 1;
            account.setLogin(fullName + addedNumber);
        }

        account.setAccessLevel(accessLevelRepository.findAccessLevelByName(accessLevel).get());
    }

    public void createAccount(Account account, String accessLevel){
        createBaseAccount(account, accessLevel);

        accountRepository.save(account);

    }

    public void createMedicalStaff(MedicalStaff medicalStaff, String accessLevel, List<String> specializations) {
        createBaseAccount(medicalStaff, accessLevel);

        List<Specialization> specializationsList = specializations.stream()
                .map(name -> specializationRepository.findSpecializationByName(name).get())
                .collect(Collectors.toList());

        medicalStaff.setSpecializations(specializationsList);
        accountRepository.save(medicalStaff);
    }
}
