package ru.dmv.cnNovosibirsk.dto.BalanceDetail;

import lombok.Data;
import ru.dmv.cnNovosibirsk.dto.AllCounters.Service;

@Data
public class ServiceInfo {
    private String mainCompany;         //  Организация РП по услуге (наименование для банка)
    private String provider;            //  Поставщик по услуге (наименование для банка)
    private Service service;            //  Услуга
    private ServiceGroup serviceGroup;  //  Группа услуг


}
