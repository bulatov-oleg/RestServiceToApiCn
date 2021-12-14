package ru.dmv.restServiceToApiCn.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.persistence.MappedSuperclass;
import java.util.Date;


@Data
@MappedSuperclass
public class AppImportMeterDataDto {

    //Примененяется в CnMeterEntity
    @JsonProperty("device_id")
    private String deviceId; //Обязательный. ид ИПУ.по нему нужно будет получать данные.
    private String name; // Обязательный. Наименование, которое отражается в приложениии Н: Санузел, ХВС
    private String inv; // серийный заводской номер ИПУ
    private String external; // строка для экспорта. Назначение непонятно
    @JsonFormat(pattern = "yyyy-MM-dd'T'hh:mm:ss'Z'")
    private Date pover; // Дата поверки
    private String pa; // Обязательный. лицевой счет
    private String codeService; // код услуги по WY
    private String nameService; // Наименование услуги по ЦН
}
