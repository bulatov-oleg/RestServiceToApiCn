package ru.dmv.restServiceToApiCn.model;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "app_meters_values")

//Таблица хранит часть данных о показаниях ИПУ, которые необходимы для работы с моб.приложением по api /exchange/import/values
// Данные храним для token, полученный из ЦН

public class AppMeterValuesEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String token;
    private double value_night;
    private double value; // значение
    private String device_id; // ид счетчика
}
