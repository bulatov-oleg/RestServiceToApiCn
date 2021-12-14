package ru.dmv.restServiceToApiCn.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.TestPropertySource;

import java.util.Base64;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("SpringJavaAutowiredMembersInspection")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = ru.dmv.lk.Application.class)
@AutoConfigureMockMvc
@TestPropertySource(locations = "file:./src/main/resources/application-dev.properties")
class ExternalRequestControllerIT {

    @SuppressWarnings("SpellCheckingInspection")
    @Value("${app.api.username}")
    private String API_USER = "apiuser";
    @Value("${app.api.password}")
    private String API_USER_PASSWORD = "EDC321&&yhn963";

    ResponseEntity<String> response;


    private TestRestTemplate restTemplate;

    @Autowired
    ExternalRequestControllerIT(TestRestTemplate restTemplate) {
        try {
            this.restTemplate = restTemplate;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void test() {
        response = invokeRequestToDmvAppSinkCn("/apiApp/test", "");
        assertEquals(response.getStatusCode(), HttpStatus.OK);
    }

    private ResponseEntity<String> invokeRequestToDmvAppSinkCn(String url, String body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization",
                "Basic " + Base64.getEncoder()
                        .encodeToString((API_USER + ":" + API_USER_PASSWORD)
                                .getBytes()));
        HttpEntity<String> request = new HttpEntity<>(body, headers);
        return restTemplate.postForEntity(url, request, String.class);
    }

    @Test
    void setCnValuesForServices() {
        response = invokeRequestToDmvAppSinkCn("/apiApp/setCnValuesForServices", "");
        assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);

        response = invokeRequestToDmvAppSinkCn("/apiApp/setCnValuesForServices", "{}");
        assertAll("Проверяем на пустой body",
                () -> assertNotNull(response.getBody(), "Null is in body"),
                () -> assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST, "Status is not BAD_REQUEST"),
                () -> assertTrue(response.getBody().contains("Список кодов услуг по ЦН в запросе должен быть"), "Message is invalid")
        );

        response = invokeRequestToDmvAppSinkCn("/apiApp/setCnValuesForServices", "{\"serviceList\":[]}");
        assertAll("Проверяем на пустой serviceList в body",
                () -> assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST, "Status is not BAD_REQUEST"),
                () -> assertTrue(Objects.requireNonNull(
                        response.getBody()).contains("Список кодов услуг по ЦН в запросе должен быть"),
                        "Message is invalid")
        );

        //проверяем на другое поле в body
        response = invokeRequestToDmvAppSinkCn("/apiApp/setCnValuesForServices", "{\"otherField\":\"data\"}");
        assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);


    }
}