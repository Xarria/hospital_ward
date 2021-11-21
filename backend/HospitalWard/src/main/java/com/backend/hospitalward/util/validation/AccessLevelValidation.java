package com.backend.hospitalward.util.validation;

import com.backend.hospitalward.model.common.AccessLevelName;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;

public class AccessLevelValidation implements ConstraintValidator<AccessLevelValid, String> {

    List<String> validValues = List.of(AccessLevelName.TREATMENT_DIRECTOR, AccessLevelName.HEAD_NURSE,
            AccessLevelName.DOCTOR, AccessLevelName.SECRETARY);

    @Override
    public boolean isValid(String accessLevel, ConstraintValidatorContext constraintValidatorContext) {

        return validValues.contains(accessLevel);
    }
}
