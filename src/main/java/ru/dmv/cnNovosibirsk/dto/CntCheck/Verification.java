package ru.dmv.cnNovosibirsk.dto.CntCheck;

import lombok.Data;

import java.util.List;

@Data
public class Verification {
    private String account; // информация о поверке для ЛС
    List<VerificationInfo> verificationInfo; //информация о поверке для ЛС
}
