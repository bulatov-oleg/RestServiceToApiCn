package ru.dmv.restServiceToApiCn.dto;

import lombok.Data;

import java.util.List;

@Data
public class AppHouseExportListDataResponceDto {
    private String all;
    List<AppHouseExportDataDto> data;
}
