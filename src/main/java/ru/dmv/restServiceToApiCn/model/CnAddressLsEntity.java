package ru.dmv.restServiceToApiCn.model;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@Table(name="cn_ls")
public class CnAddressLsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String account;
    private String adr;
    private String fias;
    private String token;
    private String info;

}
