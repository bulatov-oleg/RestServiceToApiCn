package ru.dmv.cnNovosibirsk.dto.Pays;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class PayDocument {
    private String reason;  //Обоснование из папки платежа
    private List<DocService> docServices; //   Расшифровка по услугам
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date dateVal; //Дата проводки
    private Info docInfo; //Информация о виде документа
    private Info company; //Информация об организации
    private String agent;
    private String unc; //Номер квитанции
}
