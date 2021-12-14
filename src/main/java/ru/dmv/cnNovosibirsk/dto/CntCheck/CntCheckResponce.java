package ru.dmv.cnNovosibirsk.dto.CntCheck;

import lombok.Data;
import ru.dmv.cnNovosibirsk.dto.CommonParts.AnswerStructure;

import java.util.List;

@Data
public class CntCheckResponce {
    private List<CCResult> ansResultList; //Ответы об ошибках по каждому счетчику
    private AnswerStructure answer;
   }
