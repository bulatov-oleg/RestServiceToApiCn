package ru.dmv.cnNovosibirsk.dto.HouseList;

import lombok.Data;
import ru.dmv.cnNovosibirsk.dto.CommonParts.AnswerStructure;

import java.util.List;

@Data
public class CnHouseListResponceDto {
    List<HouseList> houseList; //Список домов
    AnswerStructure answer; // Ответная структура
}
