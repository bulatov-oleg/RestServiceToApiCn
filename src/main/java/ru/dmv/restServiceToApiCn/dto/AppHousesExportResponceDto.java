package ru.dmv.restServiceToApiCn.dto;

import lombok.Data;

import java.util.List;

@Data
public class AppHousesExportResponceDto {
    boolean success;
    List<String> error;
    private AppHouseExportListDataResponceDto values;
}
