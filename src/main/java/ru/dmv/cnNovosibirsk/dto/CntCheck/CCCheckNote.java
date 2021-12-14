package ru.dmv.cnNovosibirsk.dto.CntCheck;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CCCheckNote {
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateNote; // Дата события
    private String checkCode; //    код результата проверки
                                /*Список возможных кодов (может расширяться по требованию):
                                CRASH Выход из строя
                                ADMISSION Допуск техника
                                NON_ADMISSION Недопуск техника
                                INTERVENTION Несанкционированное вмешательство в работу прибора учета
                                IND_CONTROL Снятие контрольных показаний
                                MAGNETIC_SEAL Установка антимагнитной пломбы
                                CONTROL_SEAL Установка контрольной пломбы
                                INTERVENTION_DEVICE Установка пломбы или устройства для фиксации несанкционированного вмешательства в работу прибора*/
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String checkMessage; //текстовое описание результата проверки
}
