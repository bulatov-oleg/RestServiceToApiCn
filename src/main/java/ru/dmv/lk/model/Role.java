package ru.dmv.lk.model;

import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority {
    ROLE_USER, ROLE_ADMIN, ROLE_API_USER, ACTUATOR_INFO;
//IN_CHARGE - Председатель совета дома ОТВЕТСТВЕННЫЙ!!!

    @Override
    public String getAuthority() {
        return name();
    }
}
