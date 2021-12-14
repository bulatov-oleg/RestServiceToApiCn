package ru.dmv.cnNovosibirsk.dto.CommonParts;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AnswerStructure {
    @JsonProperty("Code")
    private int Code;       //  Код результата
    @JsonProperty("Message")
    private String Message; //  Описание результатов
    @JsonProperty("Technical")
    private String Technical; //Техническая информация


}
