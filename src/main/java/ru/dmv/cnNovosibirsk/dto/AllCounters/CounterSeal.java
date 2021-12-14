package ru.dmv.cnNovosibirsk.dto.AllCounters;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.util.Date;


@Data
public class CounterSeal {
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date sealDate; // Дата опломбировки
    private String sealNumber; //  номер пломбы
    private String master; //  ФИО техника
    private String sealType; // вид пломбы
    private String place; // место установки
}
