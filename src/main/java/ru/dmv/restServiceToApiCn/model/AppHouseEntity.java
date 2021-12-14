package ru.dmv.restServiceToApiCn.model;


import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "app_house")

public class AppHouseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String id_house;
    private String address;
    private String token;
    private String info;

}
