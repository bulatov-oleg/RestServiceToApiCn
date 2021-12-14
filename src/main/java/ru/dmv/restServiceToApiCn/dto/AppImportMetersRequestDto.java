package ru.dmv.restServiceToApiCn.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import javax.persistence.MappedSuperclass;
import java.util.Date;
import java.util.List;

@Data
@MappedSuperclass
//Загрузка ИПУ в App
public class AppImportMetersRequestDto {
    // не уверен .... https://coderoad.ru/41520324/%D0%94%D0%B5%D1%81%D0%B5%D1%80%D0%B8%D0%B0%D0%BB%D0%B8%D0%B7%D1%83%D0%B9%D1%82%D0%B5-Json-%D1%81-%D0%BF%D0%BE%D0%BC%D0%BE%D1%89%D1%8C%D1%8E-%D0%BB%D0%B8%D1%82%D0%B5%D1%80%D0%B0%D0%BB%D0%B0-T
    @JsonFormat(pattern = "yyyy-MM-dd'T'hh:mm:ss'Z'")
    private Date ts; //Обязательное. дата время экспорта
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String exclude_pa; // Необязательное исключаемый лицевой счет. Все ИПУ для этого лицевого станут неактивными.
    List<AppImportMeterDataDto> data; //Обязательный. Длина от 1 до 300. Желательно отправлять ИПУ по одному лицевому в разных пакетах.

}
