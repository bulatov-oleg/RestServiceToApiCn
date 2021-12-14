package ru.dmv.cnNovosibirsk.dto.Pays;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class DocService {
    @JsonProperty("Name")
    private String Name; //Наименование услуги
    @JsonProperty("Value")
    private String Value; //Сумма платежа
    @JsonProperty("ValueType")
    private String ValueType;
    @JsonProperty("Code")
    private String Code; //Код услуги
}
