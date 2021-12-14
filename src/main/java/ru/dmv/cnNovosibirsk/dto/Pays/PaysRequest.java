package ru.dmv.cnNovosibirsk.dto.Pays;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
public class PaysRequest {
    private List<String> account; // Массив с шифрами ЛС в СН
    private String accountTypeReturn; //    Тип возвращаемого идентификатора ЛС
    // 1, пусто - шифр СН, 2 - абонентский номер
    private String token; // Система-источник. Токен, который присваивает Система начислений в справочнике "Внешние системы,
    // имеющие доступ в СН"
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date dateIn; // Указание периода "Дата с" включительно
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date dateOut; //  Указание периода "Дата по" включительно
}
