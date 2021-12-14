package ru.dmv.lk.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Set;

@Entity
@Table(name = "usr")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String username;
    private String password;
    private boolean active;

    private String email;
    private String activationCode;
    @JsonFormat(pattern = "dd.MM.yyyy")
    private LocalDate wasActive; // Дата последней активности при регистрации или входе


    public LocalDate getWasActive() {
        return wasActive;
    }

    public void setWasActive(LocalDate wasActive) {
        this.wasActive = wasActive;
    }

    @ElementCollection(targetClass = Role.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "user_role", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    private Set<Role> roles;



    public User(){}

    public boolean isAdmin() {
        return roles.contains(Role.ROLE_ADMIN);
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    @Override
    //Учетная запись, не истекшая
    //Запрашиватся при логине
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    //Запрашиватся при логине
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    //Запрашиватся при логине
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    //Запрашиватся при логине
    public boolean isEnabled() {
        return getActive();
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return getRoles();
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean getActive() {
        return this.active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getActivationCode() {
        return activationCode;
    }

    public void setActivationCode(String activationCode) {
        this.activationCode = activationCode;
    }
}