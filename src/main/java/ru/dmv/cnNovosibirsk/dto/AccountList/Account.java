package ru.dmv.cnNovosibirsk.dto.AccountList;

import lombok.Data;

@Data
public class Account {
    private String account; // шифр ЛС обязательный
    private String accountType; // 1,пусто - шифр ЦН
                                // 2 - абонентский номер
                                // 3 - справочник соответствий
                                // не обязательный
    private String companyCode; // не обязательный код организации владельца абонентского номера
}
