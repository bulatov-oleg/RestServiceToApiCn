package ru.dmv.cnNovosibirsk.dto.AccountList;

import lombok.Data;

import java.util.List;

@Data
public class AccountListRequest {
    private List<Account> accounts; // не обязательный
    private List<String> idHouse; // идентификаторы домов не обязательный
    private List<String> idFlat; // идентификаторы помещений не обязательный
    private List<String> fias; // список кодов ФИАС домов не обязательный
    private String token; //получаем из ЦН для доступа к группе домов

}
