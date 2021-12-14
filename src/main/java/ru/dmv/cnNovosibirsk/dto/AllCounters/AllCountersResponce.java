package ru.dmv.cnNovosibirsk.dto.AllCounters;

import lombok.Data;
import ru.dmv.cnNovosibirsk.dto.CommonParts.AnswerStructure;

import java.util.List;

@Data
public class AllCountersResponce {
    private List<AccCnt> accCntList;  //Массив счетчиков
    private AnswerStructure answer;
}
