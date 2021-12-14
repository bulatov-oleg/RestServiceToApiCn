package ru.dmv.restServiceToApiCn.dto;

import lombok.Data;

import javax.persistence.MappedSuperclass;

@MappedSuperclass
@Data
public class AppImportAddressDataDto {
    private String adr; // полный адрес с квартирой в формате APP
    private String pa; //лицевой счет
    private String code; //служебный код

}
