package ru.dmv.cnNovosibirsk.dto.Pays;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Info {
    @JsonProperty("FullName")
    private String FullName; // Наименование вида документа
    @JsonProperty("ShortName")
    private String ShortName; // Вид документа
    @JsonProperty("Code")
    private String Code; //  Код вида документа
    @JsonProperty("Additional")
    private String Additional;
}
