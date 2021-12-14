package ru.dmv.restServiceToApiCn.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AppExportValuesData {
    private double value; // значение
    @JsonProperty("external_id")
    private String device_id; // ид счетчика
}
