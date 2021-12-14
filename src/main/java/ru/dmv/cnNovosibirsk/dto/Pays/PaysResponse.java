package ru.dmv.cnNovosibirsk.dto.Pays;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import ru.dmv.cnNovosibirsk.dto.CommonParts.AnswerStructure;

@Data
public class PaysResponse {
    private AnswerStructure answer;
    @JsonProperty("Pays")
    private Pays Pays;
}
