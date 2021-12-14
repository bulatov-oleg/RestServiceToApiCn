package ru.dmv.cnNovosibirsk.dto.BalanceDetail;

import lombok.Data;
import org.apache.commons.lang3.builder.EqualsBuilder;
import ru.dmv.cnNovosibirsk.dto.CommonParts.AnswerStructure;

import java.util.List;

@Data

public class BalanceDetailResponse {

    private List<AccBalance> accBalanceList; //Массив информации о задолженности
    private AnswerStructure answer;


    @Override
    public boolean equals(Object compareObject) {
        if (compareObject == null) return false;
        if (!compareObject.getClass().equals(this.getClass())) return false;
        //по полям и по значениям полей
        return EqualsBuilder.reflectionEquals(this, compareObject);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}

