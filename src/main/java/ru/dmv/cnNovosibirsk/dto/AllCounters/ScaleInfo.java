package ru.dmv.cnNovosibirsk.dto.AllCounters;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;


@Data
public class ScaleInfo {
    private String scale; // Шкала (номер)
    private String scaleName; // Наименование шкалы
    private String name; // Серийный номер + название шкалы
    private Measure measure; // единицы измерения
    private int bitDepth; // Разрядность (знаков до запятой)
    private int precision; // Точность (знаков после запятой)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date indicationDate; // Дата предыдущих показаний
    private String indication; //  Предыдущие показания
    private int consumptionCoef; // Коэффициент приведения расхода
    private int maxConsumption;
    private String counterGroup; // Группа счетчиков. Показания всех счетчиков одной группы должны вноситься одновременно
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date dateOut; //   Дата следующей поверки счетчика (дата окончания счетчика)
    private String warning; //Текстовое сообщение, содержащее предупреждение о скором наступлении поверки счетчика или иное предупреждение
    private BanIndication banIndication; // Запрет ввода показаний
    private String srvExtID; // внешний код услуги
    private CounterParam counterParams; // Параметры счетчика (такие как дата производства, контактный телефон производителя)


}
