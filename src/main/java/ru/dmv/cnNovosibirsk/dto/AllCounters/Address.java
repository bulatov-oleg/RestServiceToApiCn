package ru.dmv.cnNovosibirsk.dto.AllCounters;

import lombok.Data;

import javax.persistence.MappedSuperclass;

@Data
@MappedSuperclass
public class Address {
    private String fias; // код ФИАС дома ЛС
    private String city; // Наименование населенного пункта с указанием типа - например, "Ижевск г."
    private String street; // Наименование улицы с указанием типа, если этот тип - не "улица". Пример "Коммунаров", "Красный пер."
    private String house; //  номер дома
    private String wing; //    номер корпуса дома (если есть)
    private String flat; //    номер помещения
    private String room; //    номер комнаты (если есть)
}
