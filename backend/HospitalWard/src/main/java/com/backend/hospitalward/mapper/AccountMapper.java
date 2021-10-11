package com.backend.hospitalward.mapper;

import com.backend.hospitalward.dto.request.account.AccountCreateRequest;
import com.backend.hospitalward.dto.request.medicalStaff.MedicalStaffCreateRequest;
import com.backend.hospitalward.dto.response.account.AccountGeneralResponse;
import com.backend.hospitalward.dto.response.medicalStaff.MedicalStaffGeneralResponse;
import com.backend.hospitalward.model.Account;
import com.backend.hospitalward.model.MedicalStaff;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AccountMapper {


    default AccountGeneralResponse toAccountGeneralResponse(Account account) {
        if (account instanceof MedicalStaff) {
            return mapMedicalStaff((MedicalStaff) account);
        } else {
            return mapOfficeStaff(account);
        }
    }

    @Mapping(target = "accessLevel", expression = "java(medicalStaff.getAccessLevel().getName())")
    MedicalStaffGeneralResponse mapMedicalStaff(MedicalStaff medicalStaff);

    @Mapping(target = "accessLevel", expression = "java(account.getAccessLevel().getName())")
    AccountGeneralResponse mapOfficeStaff(Account account);

    default Account toAccount(AccountCreateRequest accountCreateRequest) {
        if (accountCreateRequest instanceof MedicalStaffCreateRequest) {
            return mapMedicalStaff((MedicalStaffCreateRequest) accountCreateRequest);
        } else {
            return mapOfficeStaff(accountCreateRequest);
        }
    }

    @Mapping(target = "accessLevel", ignore = true)
    Account mapOfficeStaff(AccountCreateRequest accountCreateRequest);

    @Mapping(target = "accessLevel", ignore = true)
    @Mapping(target = "specializations", ignore = true)
    MedicalStaff mapMedicalStaff(MedicalStaffCreateRequest medicalStaffCreateRequest);
}
