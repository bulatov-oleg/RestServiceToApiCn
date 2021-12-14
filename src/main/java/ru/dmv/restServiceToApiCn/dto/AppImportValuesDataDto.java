package ru.dmv.restServiceToApiCn.dto;

import lombok.Data;

import javax.persistence.MappedSuperclass;

@Data
// Данные при загрузке показаний ИПУ
@MappedSuperclass
public class AppImportValuesDataDto {
    private String device_id; // внутренний ид устройства из которой производят экспорт
    private double value; // значение счетчика
    private double value_night; //значение счетчика

}
