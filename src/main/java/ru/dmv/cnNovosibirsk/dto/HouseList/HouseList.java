package ru.dmv.cnNovosibirsk.dto.HouseList;

import lombok.Data;

import java.util.List;

@Data
public class HouseList {
    List<AddressHouseDto> address; // адрес дома
    private String idHouse; // Идентификатор дома
}

