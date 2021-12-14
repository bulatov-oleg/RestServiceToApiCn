package ru.dmv.cnNovosibirsk.dto.AccountList;

import lombok.Data;
import ru.dmv.cnNovosibirsk.dto.CommonParts.AnswerStructure;

@Data
public class AccountListResponce {
    private String accountList; // список ЛС
    private AnswerStructure answer;
}
