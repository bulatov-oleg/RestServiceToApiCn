package ru.dmv.cnNovosibirsk.dto.Pays;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class PaysDocument {
    private String account; //Идентификатор ЛС (учитывая тип источника поиска ЛС в запросе)
    private String address; //  Адрес лицевого счета
    @JsonProperty("DocsList")
    private List<PayDocument> DocsList; //  Информация по платежам, может быть несколько элементов

}
