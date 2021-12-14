package ru.dmv.cnNovosibirsk.dto.AllCounters;

import lombok.Data;
import java.util.List;

@Data

public class AccCnt {
    private String account; //    Идентификатор ЛС
    private Address address; //   Адрес ЛС
    private List<CntInfo> cntInfo; //информация по счетчикам. Может быть несколько элементов
}
