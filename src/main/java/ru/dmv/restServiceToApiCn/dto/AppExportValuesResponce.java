package ru.dmv.restServiceToApiCn.dto;

import lombok.Data;

import java.util.List;

@Data
public class AppExportValuesResponce {
   private boolean success;
   private List<String> error;
   private AppExportValuesValues values;

}
