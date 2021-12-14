package ru.dmv.cnNovosibirsk.service;


import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public interface CnApiService {

    String getBodyResponseCn(String url, String body) throws Exception;

    ResponseEntity<String> getResponseCn(String url, String body) throws Exception;

}
