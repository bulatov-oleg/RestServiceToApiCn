package ru.dmv.cnNovosibirsk.dto.CntCheck;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import ru.dmv.cnNovosibirsk.dto.CommonParts.AnswerStructure;

import java.time.LocalDate;
import java.util.List;

@Data
public class CCResult {
    @JsonProperty("ID")
    private String ID; //Код ИПУ  Аннотация нужна, иначе приходит null при чтении json
    private List<AnswerStructure> result;
    private String account;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate indicationDate;

}
