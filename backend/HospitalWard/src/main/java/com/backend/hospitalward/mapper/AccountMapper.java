package com.backend.hospitalward.mapper;

import com.backend.hospitalward.dto.response.AccountGeneralResponse;
import com.backend.hospitalward.dto.response.MedicalStaffGeneralResponse;
import com.backend.hospitalward.model.Account;
import com.backend.hospitalward.model.MedicalStaff;
import org.mapstruct.Mapper;
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

    MedicalStaffGeneralResponse mapMedicalStaff(MedicalStaff medicalStaff);

    AccountGeneralResponse mapOfficeStaff(Account account);
}
