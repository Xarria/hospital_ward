package com.backend.hospitalward.dto;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class Credentials {

    String login;

    String password;
}
