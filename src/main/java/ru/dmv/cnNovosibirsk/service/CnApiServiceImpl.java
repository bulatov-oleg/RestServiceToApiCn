package ru.dmv.cnNovosibirsk.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;
import ru.dmv.restServiceToApiCn.exception.RestServiceToApiCnException;
import ru.dmv.cnNovosibirsk.factory.CnCustomeRestTemplateFactory;

import java.util.concurrent.TimeUnit;

@Service
public class CnApiServiceImpl implements CnApiService {

    private final Logger log = LoggerFactory.getLogger(CnApiServiceImpl.class);

    @Value("${app.cn.CnLimitRequestAttempts}")
    private int CN_LIMIT_REQUEST_ATTEMPTS;
    @Value("${app.cn.CnTimeout}")
    private int CN_REQUEST_TIMEOUT;
    @Value("${app.cn.url}")
    private String CN_URL;  //  "https://www.kvartplata.ru:4433"


    @Override
    public String getBodyResponseCn(String url, String body) throws Exception {
        return getResponseCn(url, body).getBody();
    }

    @Override
    public ResponseEntity<String> getResponseCn(String url, String body) throws Exception {
        int pause = CN_REQUEST_TIMEOUT; // пауза между запросами
        String logMessage;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = null;

        int i = 1;
        for (i = 1; i <= CN_LIMIT_REQUEST_ATTEMPTS; i++) { //попробуем до 10 раз

            TimeUnit.SECONDS.sleep(pause);

            try {
                response =
                        getCnRestTemplate(CN_URL).postForEntity(url, request, String.class);
            } catch (Exception e) {
                log.info(url + " Exception c body " + body + " при выполнении запроса на попытке " + i);
                log.info(e.getMessage() + " " + e.getCause());
//                throw new RestServiceToApiCnException(url + " Exception при выполнении запроса", null);
                continue;
            }

            if (HttpStatus.INTERNAL_SERVER_ERROR.equals(response.getStatusCode()) && i <= 10) {
                log.info(url + " c body " + body + " вернул 500 ошибку на попытке " + i);
                continue;
            }
            if (HttpStatus.OK.equals(response.getStatusCode())) {
                break;
            } else {
                logMessage = url + " c body " + body + " Ошибка, полученнная от сервера " +
                        response.getStatusCode() + " " + response.getStatusCodeValue() + ".";
                log.info(logMessage);
                throw new RestServiceToApiCnException(logMessage, null);
            }
        }
        if (response == null) {
            logMessage = url + " c body " + body + " не выполнен, вернул null.";
            log.info(logMessage);
            throw new RestServiceToApiCnException(logMessage, null);
        }
        if (i >= CN_LIMIT_REQUEST_ATTEMPTS) {
            logMessage = url + " c body " + body + " " + CN_LIMIT_REQUEST_ATTEMPTS + " неуспешных попыток ";
            log.info(logMessage);
            throw new RestServiceToApiCnException(logMessage, null);
        }

        return response;
    }

    //Вернет RestTemplate, настроенный на SSL контекст для  ЦН
    private RestOperations getCnRestTemplate(String url) throws Exception {
        RestTemplate restTemplate;
        try {
            restTemplate = new CnCustomeRestTemplateFactory(url).build();
        } catch (Exception e) {
            throw new RestServiceToApiCnException(e.getMessage(), e);
        }
        return restTemplate;
    }
}
