package ru.dmv.restServiceToApiCn.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
//Загрузка показаний ИПУ в APP
public class AppImportValuesRequestDto {
    @JsonFormat(pattern = "yyyy-MM-dd'T'hh:mm:ss'Z'")
    private Date ts; //Обязательное. дата время экспорта
    private List<AppImportValuesDataDto> data; // Данные. От 1 до 300 записей
}
