package ru.dmv.cnNovosibirsk.dto.CntCheck;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import ru.dmv.cnNovosibirsk.dto.AllCounters.CounterSeal;

import java.time.LocalDate;
import java.util.List;

@Data
public class VerificationInfo {
    @JsonProperty("ID")
    private String ID; //Код ИПУ  Аннотация нужна, иначе приходит null при чтении json
    @JsonIgnore        //Если не ввести id с этой аннотацией, то в json появляется ID и id - причина непонятна
    private String id;
    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LocalDate indicationDate;   //    Дата снятия показаний при проверке
    private List<Indication> indications; //   Показание при проверке

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<CCCheckNote> checkNotes;//    Обнаруженные неисправности и примечания
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<CCCounterParams> ccCounterParams; // измененные параметры счетчика
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<CounterSeal> counterSeals; // новые пломбы счетчика



}
