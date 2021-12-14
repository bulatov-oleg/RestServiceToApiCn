package ru.dmv.restServiceToApiCn.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import ru.dmv.restServiceToApiCn.dto.ServiceListDto;
import ru.dmv.restServiceToApiCn.exception.RestServiceToApiCnException;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SuppressWarnings("SpringJavaAutowiredMembersInspection")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = ru.dmv.lk.Application.class)
@AutoConfigureMockMvc
@TestPropertySource(locations = "file:./src/main/resources/application-test.properties")
public class SinkServiceCommonIT {
    @Autowired
    private SinkServiceCnSideAndCommonImpl sinkService;

    @Test
    void setServiceList() {
        ServiceListDto serviceListDto = new ServiceListDto(); //null SeviceListDto.getServiceList()
        assertThrows(RestServiceToApiCnException.class,
                () -> sinkService.setServiceList(serviceListDto),
                "Список кодов услуг не может быть пустым"
        );

        serviceListDto.setServiceList(new ArrayList<>()); //0 длинаSeviceListDto.getServiceList()
        assertThrows(RestServiceToApiCnException.class,
                () -> sinkService.setServiceList(serviceListDto),
                "Список кодов услуг не может быть пустым"
        );

    }
}
