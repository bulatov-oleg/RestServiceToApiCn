package ru.dmv.restServiceToApiCn.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AppImportReceiptServiceDto {
    private String name;    // "Наименование услуги"
    private long accured; // "начислено, в копейках"
    private long recalculation;  //перерасчет, в копейках
}