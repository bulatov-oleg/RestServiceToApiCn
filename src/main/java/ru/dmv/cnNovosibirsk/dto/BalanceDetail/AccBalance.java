package ru.dmv.cnNovosibirsk.dto.BalanceDetail;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class AccBalance {
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date month;         //месяц, для которого получена информация
    private List<Balance> balanceList;  //информация по задолженности
    private String account;

}
