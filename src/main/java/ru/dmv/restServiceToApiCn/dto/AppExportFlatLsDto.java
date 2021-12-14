package ru.dmv.restServiceToApiCn.dto;

import lombok.Data;

import javax.persistence.MappedSuperclass;

@Data
@MappedSuperclass //Для использовании как предка
public class AppExportFlatLsDto {
    private String flat;
    private String sc;
}
