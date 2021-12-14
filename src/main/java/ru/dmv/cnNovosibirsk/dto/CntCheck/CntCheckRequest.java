package ru.dmv.cnNovosibirsk.dto.CntCheck;

import lombok.Data;

import java.util.List;

@Data
public class CntCheckRequest {
    private List<Verification> verificationList;
    private String token; //Token пользователя
}
