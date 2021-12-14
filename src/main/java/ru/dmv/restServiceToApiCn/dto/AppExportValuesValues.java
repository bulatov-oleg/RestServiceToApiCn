package ru.dmv.restServiceToApiCn.dto;

import lombok.Data;

import java.util.List;

@Data
public class AppExportValuesValues {
    private long all; //cколько всего,
    private List<AppExportValuesData> data;
}
