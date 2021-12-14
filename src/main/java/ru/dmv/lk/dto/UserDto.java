package ru.dmv.lk.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import ru.dmv.lk.model.User;

import java.time.LocalDate;

/**
 * DTO class for user requests by ROLE_USER
 *
 * @author Eugene Suleimanov
 * @version 1.0
 */

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDto {
    private Long id;
    private String username;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDate getWasActive() {
        return wasActive;
    }

    public void setWasActive(LocalDate wasActive) {
        this.wasActive = wasActive;
    }

    private String email;
    @JsonFormat(pattern = "dd.MM.yyyy")
    private LocalDate wasActive;

    public User toUser(){
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setWasActive(wasActive);

        return user;
    }

    public static UserDto fromUser(User user) {
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setUsername(user.getUsername());
        userDto.setEmail(user.getEmail());
        userDto.setWasActive(user.getWasActive());

        return userDto;
    }
}
