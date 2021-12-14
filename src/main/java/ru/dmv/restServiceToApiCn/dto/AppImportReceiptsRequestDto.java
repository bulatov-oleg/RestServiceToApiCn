package ru.dmv.restServiceToApiCn.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class AppImportReceiptsRequestDto {
    @JsonFormat(pattern = "yyyy-MM-dd'T'hh:mm:ss'Z'")
    private Date ts; //Обязательное. дата время экспорта
    private List<AppImportReceiptsDataDto> data; // "maxItems": 300, "minItems": 1,

}
