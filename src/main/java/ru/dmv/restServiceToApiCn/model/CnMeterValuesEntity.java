package ru.dmv.restServiceToApiCn.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.dmv.restServiceToApiCn.dto.AppImportValuesDataDto;

import javax.persistence.*;
import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "cn_meters_values")

//Таблица хранит часть данных о показаниях ИПУ, которые необходимы для работы с моб.приложением по api /exchange/import/values
// Данные храним для token, полученный из ЦН

public class CnMeterValuesEntity extends AppImportValuesDataDto {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private Date indicationDate;
    private String pa; // лицевой счет. Нужен для очиски данных в таблице
    private String token;

    public CnMeterValuesEntity(){super();}

}
