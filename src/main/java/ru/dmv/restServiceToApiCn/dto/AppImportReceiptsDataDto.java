package ru.dmv.restServiceToApiCn.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class AppImportReceiptsDataDto {
    private String uuid;    //"код платежки" должен быть уникальным. По нему заменяется платежка, похоже
    private String pa;      // "лицевой счет"
    private int type;       // "тип (по умолчанию 1)"
    @JsonFormat(pattern = "yyyy-MM-dd'T'hh:mm:ss'Z'")
    private Date date;      //"дата платежки (01-месяц-год)"
    private long debt;      //"задолженность в копейках"
    private long payments;  //"внесено оплат, в копейках"
    @JsonFormat(pattern = "yyyy-MM-dd'T'hh:mm:ss'Z'")
    private Date paymentsDate;   //"дата последней оплаты"
    private long accrued;   // "начислено оплат, в копейках"
    private long total;     //"итого к оплате, в копейках"
    private List<AppImportReceiptServiceDto> service;
}
