package ru.dmv.restServiceToApiCn.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.dmv.cnNovosibirsk.dto.HouseList.AddressHouseDto;

import javax.persistence.*;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "cn_house")
@Data
// Это таблица для хранения полученных из ЦН по /HouseList адресов
// Пользователь, который с ней работает не учитывается.
// Данные храним для token, полученный из ЦН
public class CnAddressHouseEntity extends AddressHouseDto {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String token;
    private String info;
    private String appIdHouse;
    private String idHouse;

    public CnAddressHouseEntity(){super();}


    //Получить адрес по правилам для App. Это пока неверно, поскольку нужно изменение улиц и номеров домов между App и ЦН
    public  String getAddressForApp(){
        String city = getCity().trim();
        String right2Char=city.substring(city.length()-3);
        if(right2Char.equals(" г.")){
            city=city.substring(0,city.length()-3);
        }
        return city+", "+getStreet()+", д."+getHouse();
    }

}
