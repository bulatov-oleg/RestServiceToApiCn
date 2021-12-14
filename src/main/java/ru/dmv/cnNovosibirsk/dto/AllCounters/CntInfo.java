package ru.dmv.cnNovosibirsk.dto.AllCounters;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.Date;
import java.util.List;

@Data
public class CntInfo {
    @JsonProperty("ID")
    private String ID; //Код  Аннотация нужна, иначе приходит null при чтении json
    private String gisID; //ID прибора учета в ГИС ЖКХ (справочник "ГИСЖКХ. Приборы учета", колонка "Идентификатор")
    private String branch; //Название УК/РП
    private String serialNo; // Серийный номер счетчика
    private String customName; // Пользовательское наименование/Место установки/название услуги
    private Service service; // Услуга
    private int typeMPU; //    Тип прибора учета
                         //  1 - простой счетчик,
                         //  2 - счетчик с термодат.,
                         //  3 - счетчик с дополнительной шкалой
                         //  4 -  двузонный счетчик,
                         //  5 - счетчик с обратн. отчетом,
                         //  6 - трехзонный счетчик
                         //  7 - счетчик на вводе при горизонт.,
                         //  8 - счетчик на выводе при горизонт.
    private int typeCons;   //0-Интегральные счетчики,
                            //1-Интервальные счетчики
    private List<ScaleInfo> scaleInfo;
    private List<CounterSeal> counterSeals; // пломбы счетчика
    private List<CheckNote> checkNotes;     // Обнаруженные неисправности и примечания
    private String srvExtID;                // внешний код услуги
    private String counterGroup;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date dateOut;  //Дата поверки

}
