package ru.dmv.restServiceToApiCn.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.util.Date;
import java.util.List;

@Data
public class AppImportAddressRequestDto {
    @JsonFormat(pattern = "yyyy-MM-dd'T'hh:mm:ss'Z'")
    private Date ts;
    @lombok.Getter
    List<AppImportAddressDataDto> data;
}
