package ru.dmv.cnNovosibirsk.dto.AllCounters;

import lombok.Data;

@Data
public class CounterParam {
    private String code; // код параметра счетчика
    private String name; // наименование параметра счетчика
    private String valueType;   // тип значения параметра счетчика. Возможные значения:
                                // число   = "N"
                                // строка = "S"
                                // дата    =  "D"
                                // логика = "B"
    private String value; // значение параметра счетчика
}
