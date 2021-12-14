package ru.dmv.cnNovosibirsk.dto.AllCounters;
import lombok.Data;

import java.util.List;

@Data
public class AllCountersRequest {
    private String account; // Идентификатор ЛС. необязательный
    private List<String> fias; //список кодов ФИАС домов, по которым требуется информация. Если пустой - информация выгружается без ограничения по домам. не обязательный
    private int allAcc; //0 - Вернуть только ЛС с ИПУ 1 - Вернуть все ЛС, независимо от наличия ИПУ Если не поле задано, то по умолчанию 1. необязательный
    private String source; // Система-источник. Получаем от ЦН. Описывает права на доступ к домам.  Обязательный

    //Поскольку необязательные поля не должны присутствовать в body of request нужно использовать:
    //    ObjectMapper mapper = new ObjectMapper();
    //    mapper.setSerializationInclusion(Include.NON_NULL);

}