package ru.dmv.cnNovosibirsk.dto.HouseList;

import lombok.Data;

import javax.persistence.MappedSuperclass;

@Data
@MappedSuperclass //Для использовании как предка
/*@EqualsAndHashCode(callSuper = false)*/
public class AddressHouseDto{
    private String fias; // фиас дома
    private String city; // Наименование населенного пункта с указанием типа - например, "Ижевск г."
    private String street; // Наименование улицы с указанием типа, если этот тип - не "улица". Пример "Коммунаров", "Красный пер."
    private String house; // номер дома
    private String wing;// номер корпуса дома (если есть)
    private String flat;
    private String room;
    private String fullAddr; // Полный адрес строкой

}
