package ru.dmv.restServiceToApiCn.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.dmv.restServiceToApiCn.dto.AppImportAddressDataDto;

import javax.persistence.*;

@Entity
@Data
@Table(name="app_ls")
@EqualsAndHashCode(callSuper = false)
public class AppAddressLsEntity extends AppImportAddressDataDto {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String token;
    private String idHouse;
    private String info;
}
