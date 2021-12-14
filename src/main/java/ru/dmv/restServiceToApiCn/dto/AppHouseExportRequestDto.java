package ru.dmv.restServiceToApiCn.dto;

import lombok.Data;

@Data
public class AppHouseExportRequestDto {
    private int limit; //сколько за раз можно вывести, по умолчанию - 300
    private int offset; //с какой записи начать - это чтоб кусками могли вытаскивать
}
