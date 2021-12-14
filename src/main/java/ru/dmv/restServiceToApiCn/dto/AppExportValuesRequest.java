package ru.dmv.restServiceToApiCn.dto;

import lombok.Data;

@Data
public class AppExportValuesRequest {
    private int limit;   //  сколько за раз можно вывести,
    private int offset;  // смещение - типа с какой записи начать - это чтоб кусками могли вытаскивать
    private int onlyNew; // 0 берет последние значения без учета пользователь их ввел или импортированы были, 1 - только пользовательские будет отдавать.
    private int month;   // месяц 1-12, за какой просим показания, если не указан или 0 - то отдаст за текущий
    private int house;   // получить показания по конкретному ид дома - если 0 - то не учитывается
    private int night;   // получить ночные показания - если 0 - то это дневные, если 1 - то ночные

    public AppExportValuesRequest(int limit, int offset, int onlyNew, int month, int house, int night) {
        this.limit = limit;
        this.offset = offset;
        this.onlyNew = onlyNew;
        this.month = month;
        this.house = house;
        this.night = night;
    }
}
