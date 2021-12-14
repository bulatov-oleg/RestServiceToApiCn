package ru.dmv.restServiceToApiCn.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import ru.dmv.restServiceToApiCn.TestUtils.InvokerProtectedMethod;
import ru.dmv.restServiceToApiCn.dto.AppExportFlatsResponseDto;
import ru.dmv.restServiceToApiCn.dto.AppImportReceiptsRequestDto;
import ru.dmv.lk.factories.ObjectMapperFactory;
import ru.dmv.lk.utils.MyDate;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("SpringJavaAutowiredMembersInspection")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = ru.dmv.lk.Application.class)
@AutoConfigureMockMvc
@TestPropertySource(locations = "file:./src/main/resources/application-test.properties")
class SinkServiceAppSideIT {
    @Autowired
    MyDate myDate;
    private AppExportFlatsResponseDto appExportFlatsResponseDto;
    private Object[] params;
    private AppImportReceiptsRequestDto actualAppImportReceiptsRequestDto;
    @Autowired
    private SinkServiceAppSideImpl sinkService;
    @Autowired
    private ObjectMapperFactory objectMapper;

    @Test
    void getInstance() {
        SinkServiceAppSide i1 = sinkService.getInstance();
        SinkServiceAppSide i2 = sinkService.getInstance();
        assertNotEquals(i1, i2);
    }

    @Test
    void getAppExportFlatsResponseForId_house() throws Exception {
        String id_house = "99999999999999"; //несуществующий
        params = new Object[]{id_house};

        assertAll("Ожидаем возвращение пустого списка квартир",
                () -> assertDoesNotThrow(() -> appExportFlatsResponseDto = (AppExportFlatsResponseDto) new InvokerProtectedMethod().
                        invokeProtectedMethod(sinkService, "getAppExportFlatsResponseForId_house", params)),
                () -> assertNotNull(appExportFlatsResponseDto),
                () -> assertEquals(0, appExportFlatsResponseDto.getFlats().size())
        );

        id_house = "2872"; //существующий
        params = new Object[]{id_house};
        try {
            appExportFlatsResponseDto = (AppExportFlatsResponseDto) new InvokerProtectedMethod().
                    invokeProtectedMethod(sinkService, "getAppExportFlatsResponseForId_house", params);
        } catch (Exception e) {
            assertTrue(false == true, "Exception in getAppExportFlatsResponseForId_house() " +
                    e.getCause() + " " + e.getMessage());
        }
        assertAll("Ожидаем возвращение непустого списка квартир",
                () -> assertDoesNotThrow(() -> appExportFlatsResponseDto = (AppExportFlatsResponseDto) new InvokerProtectedMethod()
                        .invokeProtectedMethod(sinkService, "getAppExportFlatsResponseForId_house", params)),
                () -> assertNotNull(appExportFlatsResponseDto),
                () -> assertNotEquals(0, appExportFlatsResponseDto.getFlats().size())
        );
    }

}
