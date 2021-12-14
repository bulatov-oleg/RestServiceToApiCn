package ru.dmv.cnNovosibirsk.factory;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CnGetTokenFactory {

    @Value("${app.cn.token}")
    private String token; //Пока так, потом подумаем как использовать для нескольких компаний

    public String getToken() throws Exception {
            return token;
    }

}
