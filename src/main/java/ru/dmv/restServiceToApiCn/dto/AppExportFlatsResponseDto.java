package ru.dmv.restServiceToApiCn.dto;

import lombok.Data;

import java.util.List;

@Data
public class AppExportFlatsResponseDto {
    List<AppExportFlatLsDto> flats;
}
