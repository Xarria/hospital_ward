package com.backend.hospitalward.mapper;

import com.backend.hospitalward.dto.request.account.AccountCreateRequest;
import com.backend.hospitalward.dto.request.account.AccountUpdateRequest;
import com.backend.hospitalward.dto.request.medicalStaff.MedicalStaffCreateRequest;
import com.backend.hospitalward.dto.request.medicalStaff.MedicalStaffUpdateRequest;
import com.backend.hospitalward.dto.response.account.AccountDetailsDTO;
import com.backend.hospitalward.dto.response.account.AccountGeneralDTO;
import com.backend.hospitalward.dto.response.medicalStaff.MedicalStaffDetailsDTO;
import com.backend.hospitalward.dto.response.medicalStaff.MedicalStaffGeneralDTO;
import com.backend.hospitalward.model.Account;
import com.backend.hospitalward.model.MedicalStaff;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AccountMapper {


    default AccountGeneralDTO toAccountGeneralResponse(Account account) {
        if (account instanceof MedicalStaff) {
            return mapMedicalStaff((MedicalStaff) account);
        } else {
            return mapOfficeStaff(account);
        }
    }

    @Mapping(target = "accessLevel", expression = "java(medicalStaff.getAccessLevel().getName())")
    MedicalStaffGeneralDTO mapMedicalStaff(MedicalStaff medicalStaff);

    @Mapping(target = "accessLevel", expression = "java(account.getAccessLevel().getName())")
    AccountGeneralDTO mapOfficeStaff(Account account);

    default AccountDetailsDTO toAccountDetailsResponse(Account account) {
        if (account instanceof MedicalStaff) {
            return mapDetailsMedicalStaff((MedicalStaff) account);
        } else {
            return mapDetailsOfficeStaff(account);
        }
    }

    @Mapping(target = "accessLevel", expression = "java(medicalStaff.getAccessLevel().getName())")
    MedicalStaffDetailsDTO mapDetailsMedicalStaff(MedicalStaff medicalStaff);

    @Mapping(target = "accessLevel", expression = "java(account.getAccessLevel().getName())")
    AccountDetailsDTO mapDetailsOfficeStaff(Account account);

    default Account toAccount(AccountCreateRequest accountCreateRequest) {
        if (accountCreateRequest instanceof MedicalStaffCreateRequest) {
            return mapMedicalStaff((MedicalStaffCreateRequest) accountCreateRequest);
        } else {
            return mapOfficeStaff(accountCreateRequest);
        }
    }

    @Mapping(target = "accessLevel", ignore = true)
    Account mapOfficeStaff(AccountCreateRequest accountCreateRequest);

    @Mapping(target = "specializations", ignore = true)
    MedicalStaff mapMedicalStaff(MedicalStaffCreateRequest medicalStaffCreateRequest);

    default Account toAccount(AccountUpdateRequest accountUpdateRequest) {
        if (accountUpdateRequest instanceof MedicalStaffUpdateRequest) {
            return mapMedicalStaff((MedicalStaffUpdateRequest) accountUpdateRequest);
        } else {
            return mapOfficeStaff(accountUpdateRequest);
        }
    }

    Account mapOfficeStaff(AccountUpdateRequest accountUpdateRequest);

    @Mapping(target = "specializations", ignore = true)
    MedicalStaff mapMedicalStaff(MedicalStaffUpdateRequest medicalStaffUpdateRequest);
}
