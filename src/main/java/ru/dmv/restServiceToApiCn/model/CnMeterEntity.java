package ru.dmv.restServiceToApiCn.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.dmv.restServiceToApiCn.dto.AppImportMeterDataDto;
import ru.dmv.lk.utils.MyDate;

import javax.persistence.*;

@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Table(name = "cn_meters")
//Таблица хранит часть данных об ИПУ, которые необходимы для работы с моб.приложением по api /exchange/import/devices
// Данные храним для token, полученный из ЦН
public class CnMeterEntity extends AppImportMeterDataDto {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String token;
    private String info;
    private int scales; //количество шкал

    public String getNameAndPoverForApp() {
        String name = this.getName().trim() +
                ". До " + new MyDate().dtocShort(this.getPover()) + " поверен";
        return name.length() <= 50 ? name : name.substring(0, 49);
    }
}

