package ru.dmv.restServiceToApiCn.dto;

import lombok.Data;

@Data
public class AppValuesWithPa {
    private String device_id;
    private double value;
    private double valueNight;
    private String pa;
    private int scales;
    private String codeService;
    private String nameService;

    public AppValuesWithPa(String device_id, double value, double valueNight, String pa, int scales,
                           String codeService, String nameService) {
        this.device_id = device_id;
        this.value = value;
        this.valueNight = valueNight;
        this.pa = pa;
        this.scales = scales;
        this.codeService=codeService;
        this.nameService=nameService;
    }


}
