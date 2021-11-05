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
import com.backend.hospitalward.model.Specialization;
import org.mapstruct.*;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface AccountMapper {


    @Named("mapModifiedBy")
    default String mapModifiedBy(Account account) {
        if(account.getModifiedBy() != null) {
            return account.getModifiedBy().getName();
        }
        return null;
    }

    @Named("mapAccessLevel")
    default String mapAccessLevel(Account account) {
        if(account.getAccessLevel() != null) {
            return account.getAccessLevel().getName();
        }
        return null;
    }

    @Named("mapCreatedBy")
    default String mapCreatedBy(Account account) {
        if(account.getCreatedBy() != null) {
            return account.getCreatedBy().getName();
        }
        return null;
    }

    @Named("mapSpecializations")
    default List<String> mapSpecializations(MedicalStaff medicalStaff) {
        if(!medicalStaff.getSpecializations().isEmpty()) {
            return medicalStaff.getSpecializations().stream().map(Specialization::getName).collect(Collectors.toList());
        }
        return null;
    }

    default AccountGeneralDTO toAccountGeneralResponse(Account account) {
        if (account instanceof MedicalStaff) {
            return mapMedicalStaff((MedicalStaff) account);
        } else {
            return mapOfficeStaff(account);
        }
    }

    @Mapping(target = "accessLevel", source = "medicalStaff", qualifiedByName = "mapAccessLevel")
    MedicalStaffGeneralDTO mapMedicalStaff(MedicalStaff medicalStaff);

    @Mapping(target = "accessLevel", source = "account", qualifiedByName = "mapAccessLevel")
    AccountGeneralDTO mapOfficeStaff(Account account);

    default AccountDetailsDTO toAccountDetailsResponse(Account account) {
        if (account instanceof MedicalStaff) {
            return mapDetailsMedicalStaff((MedicalStaff) account);
        } else {
            return mapDetailsOfficeStaff(account);
        }
    }

    @Mapping(target = "accessLevel", source = "medicalStaff", qualifiedByName = "mapAccessLevel")
    @Mapping(target = "createdBy", source = "medicalStaff", qualifiedByName = "mapCreatedBy")
    @Mapping(target = "modifiedBy", source = "medicalStaff", qualifiedByName = "mapModifiedBy")
    @Mapping(target = "specializations", source = "medicalStaff", qualifiedByName = "mapSpecializations")
    MedicalStaffDetailsDTO mapDetailsMedicalStaff(MedicalStaff medicalStaff);

    @Mapping(target = "accessLevel", source = "account", qualifiedByName = "mapAccessLevel")
    @Mapping(target = "createdBy", source = "account", qualifiedByName = "mapCreatedBy")
    @Mapping(target = "modifiedBy", source = "account", qualifiedByName = "mapModifiedBy")
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

    @Mapping(target = "accessLevel", ignore = true)
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
