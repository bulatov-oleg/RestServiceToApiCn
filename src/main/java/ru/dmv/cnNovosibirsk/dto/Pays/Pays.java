package ru.dmv.cnNovosibirsk.dto.Pays;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class Pays {
    @JsonProperty("PaysDocuments")
    private List<PaysDocument> PaysDocuments; //Информация по ЛС (в ответе может быть несколько ЛС, если в запросе не указан конкретный ЛС)
}
