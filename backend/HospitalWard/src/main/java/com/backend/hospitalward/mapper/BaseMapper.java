package com.backend.hospitalward.mapper;

import com.backend.hospitalward.model.Account;
import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface BaseMapper {

    @Named("mapModifiedBy")
    default String mapModifiedBy(Account account) {
        if (account.getModifiedBy() != null) {
            return account.getModifiedBy().getName();
        }
        return null;
    }

    @Named("mapCreatedBy")
    default String mapCreatedBy(Account account) {
        if (account.getCreatedBy() != null) {
            return account.getCreatedBy().getName();
        }
        return null;
    }
}
