package ru.dmv.restServiceToApiCn.service;


import com.fasterxml.jackson.databind.DeserializationFeature;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import ru.dmv.restServiceToApiCn.TestUtils.InvokerProtectedMethod;
import ru.dmv.restServiceToApiCn.dto.AppImportReceiptServiceDto;
import ru.dmv.restServiceToApiCn.dto.AppImportReceiptsDataDto;
import ru.dmv.restServiceToApiCn.dto.AppImportReceiptsRequestDto;
import ru.dmv.restServiceToApiCn.dto.ServiceListDto;
import ru.dmv.restServiceToApiCn.exception.RestServiceToApiCnException;
import ru.dmv.restServiceToApiCn.model.AppMeterValuesEntity;
import ru.dmv.restServiceToApiCn.model.CnAddressHouseEntity;
import ru.dmv.restServiceToApiCn.model.CnMeterEntity;
import ru.dmv.restServiceToApiCn.repository.AppMetersValuesRepo;
import ru.dmv.restServiceToApiCn.repository.CnAddressHouseRepo;
import ru.dmv.restServiceToApiCn.repository.CnMeterRepo;
import ru.dmv.cnNovosibirsk.dto.BalanceDetail.BalanceDetailResponse;
import ru.dmv.cnNovosibirsk.dto.Pays.PaysResponse;
import ru.dmv.cnNovosibirsk.factory.CnGetTokenFactory;
import ru.dmv.lk.factories.ObjectMapperFactory;
import ru.dmv.lk.utils.MyDate;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("SpringJavaAutowiredMembersInspection")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = ru.dmv.lk.Application.class)
@AutoConfigureMockMvc
@TestPropertySource(locations = "file:./src/main/resources/application-test.properties")
class SinkServiceCnSideIT {

    //поля класса, поскольку применяюся в лямбдах
    private String rBody;
    private List<CnAddressHouseEntity> cnAddressHouseEntities;
    private String message;
    private BalanceDetailResponse balanceDetailResponse;
    private AppImportReceiptsRequestDto appImportReceiptsRequestDto;
    private Object[] params;
    private Map<String, Date> cnLastPays;
    private PaysResponse paysResponse;
    private AppImportReceiptsRequestDto actualAppImportReceiptsRequestDto;


    /*
        @Autowired
        private MockMvc mvc;
    */
    @Autowired
    private SinkServiceCnSideAndCommonImpl sinkService;
    @Autowired
    private ObjectMapperFactory objectMapper;
    @Autowired
    private CnAddressHouseRepo cnAddressHouseRepo;
    @Autowired
    private Environment env;
    @Autowired
    private CnGetTokenFactory tokenFactory;
    @Autowired
    private AppMetersValuesRepo appMetersValuesRepo;
    @Autowired
    private CnMeterRepo cnMeterRepo;
    @Autowired
    private MyDate myDate;


    // write test cases here
    @Test
    @Transactional
    void setCnValue() throws Exception {

        ServiceListDto serviceListDto = new ServiceListDto();
        serviceListDto.setServiceList(new ArrayList<>());

        //Создаем ИПУ из ЦН
        String deviceId = "qwerty";
        String codeServiceRigth = "22222222222222222222"; //верная, но несуществующая
        cnMeterRepo.deleteAllByToken(tokenFactory.getToken());
        CnMeterEntity cnMeterEntity = new CnMeterEntity();
        cnMeterEntity.setToken(tokenFactory.getToken());
        cnMeterEntity.setPa("12345678912345");
        cnMeterEntity.setCodeService(codeServiceRigth);
        cnMeterEntity.setDeviceId(deviceId);
        cnMeterRepo.saveAndFlush(cnMeterEntity);

        //создаем показания для этого ИПУ,
        appMetersValuesRepo.deleteAllByToken(tokenFactory.getToken());
        AppMeterValuesEntity appMeterValuesEntity = new AppMeterValuesEntity();
        appMeterValuesEntity.setDevice_id(deviceId);
        appMeterValuesEntity.setToken(tokenFactory.getToken());
        appMeterValuesEntity.setValue(3.0);
        appMeterValuesEntity.setValue_night(0.0);
        appMetersValuesRepo.saveAndFlush(appMeterValuesEntity);

        serviceListDto.getServiceList().add("111111111аааывываыв"); //Передаем заведомо неверный код услуги
        //ожидаем, что будет передано 0 показаний
        sinkService.setWhatDo("for");
        sinkService.setServiceList(serviceListDto);
        assertAll("Одна запись с неверной услугой",
                () -> assertDoesNotThrow(() -> message = sinkService.setCnValues()),
                () -> assertTrue(message.contains(" Закончили передачу в ЦН.")),
                () -> assertTrue(message.contains("Всего выбрано для передачи 0"))
        );
        System.out.println(System.lineSeparator() + "Возвращено методом setCnValues(): " + System.lineSeparator() + message);


        serviceListDto.getServiceList().clear();
        serviceListDto.getServiceList().add(codeServiceRigth);
        sinkService.setServiceList(serviceListDto);
        assertAll("Одна запись с верной несуществующей услугой",
                () -> assertDoesNotThrow(() -> message = sinkService.setCnValues()),
                () -> assertTrue(message.contains(" Закончили передачу в ЦН.")),
                () -> assertTrue(message.contains("Всего выбрано для передачи 1"))
                //cntCheck() внутри setCnValues() вернул - без ошибок. Что неверно!!!
        );
        System.out.println(System.lineSeparator() + "Возвращено методом setCnValues(): " + System.lineSeparator() + message);
    }

    @Test
    void setReceiptsFromAppToCn() throws Exception {

        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        //Для проверяем ожидаемый ответ для данных
        List<String> account = Arrays.asList("0229074731");
        Date month = myDate.StringYyyyMmDdToDate(2021, 9, 30);
        params = new Object[]{account, month};

        assertAll("BalanceDetailResponse - должен иметь структуру описанную в BalanceDetailResponse.class, иначе получим Exception по json",
                () -> assertDoesNotThrow(() -> balanceDetailResponse = (BalanceDetailResponse) new InvokerProtectedMethod()
                        .invokeProtectedMethod(sinkService, "getCnBalanceDetailResponse", params)),
                () -> assertNotNull(balanceDetailResponse)
        );
        //Тест сработает, если не совпадут схемы json
        //Не сработает, если не придет какое-то поле, ему просто присвоится null.
        params = new Object[]{account, month};

        assertAll("PaysResponse - должен иметь структуру описанную в PaysResponse .class, иначе получим Exception по json",
                () -> assertDoesNotThrow(() -> paysResponse = (PaysResponse) new InvokerProtectedMethod()
                        .invokeProtectedMethod(sinkService, "getCnPaysResponse", params)),
                () -> assertNotNull(paysResponse)
        );
        params = new Object[]{paysResponse};
        assertAll("",
                () -> assertDoesNotThrow(() -> cnLastPays = (Map<String, Date>) new InvokerProtectedMethod()
                        .invokeProtectedMethod(sinkService, "getCnLastPays", params)),
                () -> assertNotNull(cnLastPays)
        );

        params = new Object[]{balanceDetailResponse, cnLastPays};
        assertAll("appImportReceiptsRequestDto - должен содержать конкретные данные",
                () -> assertDoesNotThrow(() -> appImportReceiptsRequestDto = (AppImportReceiptsRequestDto) new InvokerProtectedMethod()
                        .invokeProtectedMethod(sinkService, "getAppImportReceiptsRequestDto", params)),
                () -> assertNotNull(appImportReceiptsRequestDto),
                () -> assertEquals(122357, appImportReceiptsRequestDto.getData().get(0).getTotal()),
                () -> assertEquals(118435, appImportReceiptsRequestDto.getData().get(0).getDebt()),
                () -> assertEquals(-118435, appImportReceiptsRequestDto.getData().get(0).getPayments()),
                () -> assertEquals(115910, appImportReceiptsRequestDto.getData().get(0).getAccrued()),
                () -> assertNotNull(appImportReceiptsRequestDto.getData().get(0).getPaymentsDate())
        );

        objectMapper = new ObjectMapperFactory();
    }


    @Test
    @Transactional
    void getAddressHouses() throws Exception {

        String token = tokenFactory.getToken();
        cnAddressHouseRepo.deleteAddressHouseCnEntitiesByToken(token);
        //реакция на пустой список домов, полученных из Cn
        assertThrows(RestServiceToApiCnException.class,
                () -> sinkService.getAddressHouses()
        );
        //Проверяем правильность сортировки при выдаче списка домов,
        // полученного из ЦН
        cnAddressHouseRepo.deleteAddressHouseCnEntitiesByToken(token);
        CnAddressHouseEntity cnAddressHouseEntity = new CnAddressHouseEntity();
        cnAddressHouseEntity.setToken(token);
        cnAddressHouseEntity.setCity("city");
        cnAddressHouseEntity.setStreet("Street2");
        cnAddressHouseEntity.setHouse("10");
        cnAddressHouseRepo.save(cnAddressHouseEntity);
        cnAddressHouseEntity = new CnAddressHouseEntity();
        cnAddressHouseEntity.setToken(token);
        cnAddressHouseEntity.setCity("city");
        cnAddressHouseEntity.setStreet("Street1");
        cnAddressHouseEntity.setHouse("1");
        cnAddressHouseRepo.save(cnAddressHouseEntity);
        cnAddressHouseEntity = new CnAddressHouseEntity();
        cnAddressHouseEntity.setToken(token);
        cnAddressHouseEntity.setCity("city");
        cnAddressHouseEntity.setStreet("Street1");
        cnAddressHouseEntity.setHouse("2");
        cnAddressHouseRepo.saveAndFlush(cnAddressHouseEntity);
        assertAll("",
                () -> assertDoesNotThrow(() -> cnAddressHouseEntities = sinkService.getAddressHouses()),
                () -> assertEquals("1", cnAddressHouseEntities.get(0).getHouse()),
                () -> assertEquals("Street1", cnAddressHouseEntities.get(0).getStreet())
        );

    }


    @Test
        //проверяем корректность формирования DTO AppImportReceiptsRequestDto
        //из BalanceDetailResponse из ЦН
    void getAppImportReceiptsRequestDto() throws Exception {
        BalanceDetailResponse balanceDetailResponse = objectMapper.readValue(getJsonBalanceDetailResponse(),
                BalanceDetailResponse.class);
        Map<String, Date> cnLastPays = new HashMap<>();
        cnLastPays.put("0229077238", myDate.nowDate());
        AppImportReceiptsRequestDto expectedDto = getExpectedAppImportReceiptsRequestDto();
        params = new Object[]{balanceDetailResponse, cnLastPays};

        assertDoesNotThrow(() -> actualAppImportReceiptsRequestDto =
                (AppImportReceiptsRequestDto) new InvokerProtectedMethod()
                        .invokeProtectedMethod(sinkService, "getAppImportReceiptsRequestDto", params));

        expectedDto.setTs(actualAppImportReceiptsRequestDto.getTs()); //Потому что это время запрса, а оно разное
        expectedDto.getData().get(0).setDate(actualAppImportReceiptsRequestDto.getData().get(0).getDate());
        expectedDto.getData().get(0).setPaymentsDate(actualAppImportReceiptsRequestDto.getData().get(0).getPaymentsDate());
        expectedDto.getData().get(0).setUuid(actualAppImportReceiptsRequestDto.getData().get(0).getUuid());
        assertEquals(expectedDto, actualAppImportReceiptsRequestDto);

        balanceDetailResponse.getAccBalanceList().get(0).setBalanceList(new ArrayList<>());
        assertAll("Нет данных об услугах на лицевом счете. Пустой массив услуг",
                () -> assertDoesNotThrow(() -> actualAppImportReceiptsRequestDto =
                        (AppImportReceiptsRequestDto) new InvokerProtectedMethod()
                                .invokeProtectedMethod(sinkService, "getAppImportReceiptsRequestDto", params)),
                () -> assertEquals("Нет данных.", actualAppImportReceiptsRequestDto.getData().get(0)
                        .getService().get(0).getName())
        );

        balanceDetailResponse.getAccBalanceList().get(0).setBalanceList(null);
        assertAll("Нет данных об услугах на лицевом счете. Null в массиве услуг",
                () -> assertDoesNotThrow(() -> actualAppImportReceiptsRequestDto =
                        (AppImportReceiptsRequestDto) new InvokerProtectedMethod()
                                .invokeProtectedMethod(sinkService, "getAppImportReceiptsRequestDto", params)),
                () -> assertEquals("Нет данных.", actualAppImportReceiptsRequestDto.getData().get(0)
                        .getService().get(0).getName())
        );

        balanceDetailResponse.setAccBalanceList(new ArrayList<>());
        assertAll("Нет данных о лиц. счетах. Пустой в массив лиц. счетов",
                () -> assertDoesNotThrow(() -> actualAppImportReceiptsRequestDto =
                        (AppImportReceiptsRequestDto) new InvokerProtectedMethod()
                                .invokeProtectedMethod(sinkService, "getAppImportReceiptsRequestDto", params)),
                () -> assertNull(actualAppImportReceiptsRequestDto.getTs()),
                () -> assertEquals(0, actualAppImportReceiptsRequestDto.getData().size())
        );

    }

    private AppImportReceiptsRequestDto getExpectedAppImportReceiptsRequestDto() throws Exception {
        //написано на основе getJsonBalanceDetailResponse()
        // с учетом удаления 0 сумм
        AppImportReceiptsRequestDto appImportReceiptsRequestDto = new AppImportReceiptsRequestDto();
        AppImportReceiptsDataDto appImportReceiptsDataDto = new AppImportReceiptsDataDto();
        appImportReceiptsDataDto.setDebt(2309423); //sum saldoin
        appImportReceiptsDataDto.setAccrued(98208); // sum charge
        appImportReceiptsDataDto.setPayments(79354); // sum payment
        appImportReceiptsDataDto.setTotal(2309423 + 98208 + 79354 + 1000 - 5164 + 1076 - 2000);
        //сальдо на начало + начислено + оплаты (отриц)+перерасчеты+пени+корректировка сальдо

        appImportReceiptsDataDto.setUuid("0229077238-" + "20210401");
        appImportReceiptsDataDto.setPa("0229077238");
        appImportReceiptsDataDto.setPaymentsDate(null);
        appImportReceiptsDataDto.setDate(null);
        appImportReceiptsDataDto.setType(1);

        List<AppImportReceiptServiceDto> appImportReceiptServiceDtos = new ArrayList<>();
        appImportReceiptServiceDtos.add(new AppImportReceiptServiceDto("КР НА СОИ (ГВС)", 1732, 1000));
        appImportReceiptServiceDtos.add(new AppImportReceiptServiceDto("КР НА СОИ (ВОДООТВЕДЕНИЕ)", 2580, 0));
        appImportReceiptServiceDtos.add(new AppImportReceiptServiceDto("КР НА СОИ (ЭЛ. ЭНЕРГИЯ)", 5164, -5164));
        appImportReceiptServiceDtos.add(new AppImportReceiptServiceDto("СОДЕРЖ. ДОМОВЛ.", 83420, 0));
        appImportReceiptServiceDtos.add(new AppImportReceiptServiceDto("СОДЕРЖАНИЕ СПЕЦ.СЧЕТА", 3153, 0));
        appImportReceiptServiceDtos.add(new AppImportReceiptServiceDto("КР НА СОИ (ВОДОСНАБЖЕНИЕ)", 2159, 0));
        appImportReceiptServiceDtos.add(new AppImportReceiptServiceDto("ПЕНИ", 1076, 0));
        appImportReceiptServiceDtos.add(new AppImportReceiptServiceDto("КОРР.САЛЬДО", -2000, 0));

        appImportReceiptsDataDto.setService(appImportReceiptServiceDtos);

        List<AppImportReceiptsDataDto> appImportReceiptsDataDtos = new ArrayList<>();
        appImportReceiptsDataDtos.add(appImportReceiptsDataDto);

        appImportReceiptsRequestDto.setData(appImportReceiptsDataDtos);
        appImportReceiptsRequestDto.setTs(null); //set будет позже
        return appImportReceiptsRequestDto;
    }

    String getJsonBalanceDetailResponse() {
        return "{\n" +
                "    \"answer\": {\n" +
                "        \"Message\": null,\n" +
                "        \"Code\": 0,\n" +
                "        \"Technical\": null\n" +
                "    },\n" +
                "    \"accBalanceList\": [\n" +
                "        {\n" +
                "            \"month\": \"2021-04-01\",\n" +
                "            \"balanceList\": [\n" +
                "                {\n" +
                "                    \"serviceInfo\": {\n" +
                "                        \"provider\": \"АО \\\"ЭНЕРГОСБЫТ ПЛЮС\\\"\",\n" +
                "                        \"service\": {\n" +
                "                            \"name\": \"ЭЛЕКТРОЭНЕРГИЯ\",\n" +
                "                            \"code\": \"010\"\n" +
                "                        },\n" +
                "                        \"mainCompany\": \"АО \\\"ЭНЕРГОСБЫТ ПЛЮС\\\"\",\n" +
                "                        \"serviceGroup\": {\n" +
                "                            \"name\": null,\n" +
                "                            \"code\": null\n" +
                "                        }\n" +
                "                    },\n" +
                "                    \"saldoin\": \"0\",\n" +
                "                    \"setSum\": \"0\",\n" +
                "                    \"charge\": \"0\",\n" +
                "                    \"payment\": \"0\",\n" +
                "                    \"futurePays\": \"0\",\n" +
                "                    \"saldoout\": \"0\",\n" +
                "                    \"forPaymentPast\": \"0\",\n" +
                "                    \"forPayment\": \"0\",\n" +
                "                    \"saldoInFine\": \"0\",\n" +
                "                    \"fineCalc\": null,\n" +
                "                    \"prepaysFine\": \"0\",\n" +
                "                    \"saldooutFine\": \"0\",\n" +
                "                    \"maket\": \"0\",\n" +
                "                    \"fine\": \"0\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"serviceInfo\": {\n" +
                "                        \"provider\": \"ООО \\\"ГУБАХИНСКАЯ ЭНЕРГЕТИЧЕСКАЯ КОМПАНИЯ\\\"\",\n" +
                "                        \"service\": {\n" +
                "                            \"name\": \"ГОР. ВОДА\",\n" +
                "                            \"code\": \"004\"\n" +
                "                        },\n" +
                "                        \"mainCompany\": \"ООО \\\"ГУБАХИНСКАЯ ЭНЕРГЕТИЧЕСКАЯ КОМПАНИЯ\\\"\",\n" +
                "                        \"serviceGroup\": {\n" +
                "                            \"name\": null,\n" +
                "                            \"code\": null\n" +
                "                        }\n" +
                "                    },\n" +
                "                    \"saldoin\": \"0\",\n" +
                "                    \"setSum\": \"0\",\n" +
                "                    \"charge\": \"0\",\n" +
                "                    \"payment\": \"0\",\n" +
                "                    \"futurePays\": \"0\",\n" +
                "                    \"saldoout\": \"0\",\n" +
                "                    \"forPaymentPast\": \"0\",\n" +
                "                    \"forPayment\": \"0\",\n" +
                "                    \"saldoInFine\": \"0\",\n" +
                "                    \"fineCalc\": null,\n" +
                "                    \"prepaysFine\": \"0\",\n" +
                "                    \"saldooutFine\": \"0\",\n" +
                "                    \"maket\": \"0\",\n" +
                "                    \"fine\": \"0\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"serviceInfo\": {\n" +
                "                        \"provider\": \"ООО \\\"ГУБАХИНСКАЯ ЭНЕРГЕТИЧЕСКАЯ КОМПАНИЯ\\\"\",\n" +
                "                        \"service\": {\n" +
                "                            \"name\": \"КР НА СОИ (ГВС)\",\n" +
                "                            \"code\": \"_04\"\n" +
                "                        },\n" +
                "                        \"mainCompany\": \"OOO \\\"ДОМОВОЙ\\\"\",\n" +
                "                        \"serviceGroup\": {\n" +
                "                            \"name\": null,\n" +
                "                            \"code\": null\n" +
                "                        }\n" +
                "                    },\n" +
                "                    \"saldoin\": \"545.97\",\n" +
                "                    \"setSum\": \"0\",\n" +
                "                    \"charge\": \"17.32\",\n" +
                "                    \"payment\": \"16.46\",\n" +
                "                    \"futurePays\": \"-2.71\",\n" +
                "                    \"saldoout\": \"538.65\",\n" + //с учетом 10 в maket
                "                    \"forPaymentPast\": \"545.97\",\n" +
                "                    \"forPayment\": \"17.32\",\n" +
                "                    \"saldoInFine\": \"32.6\",\n" +
                "                    \"fineCalc\": null,\n" +
                "                    \"prepaysFine\": \"0\",\n" +
                "                    \"saldooutFine\": \"32.76\",\n" +
                "                    \"maket\": \"10.00\",\n" + // внес руками
                "                    \"fine\": \".16\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"serviceInfo\": {\n" +
                "                        \"provider\": \"МУП Г. САРАПУЛА \\\"САРАПУЛЬСКИЙ ВОДОКАНАЛ\\\"\",\n" +
                "                        \"service\": {\n" +
                "                            \"name\": \"КР НА СОИ (ВОДООТВЕДЕНИЕ)\",\n" +
                "                            \"code\": \"30_\"\n" +
                "                        },\n" +
                "                        \"mainCompany\": \"OOO \\\"ДОМОВОЙ\\\"\",\n" +
                "                        \"serviceGroup\": {\n" +
                "                            \"name\": null,\n" +
                "                            \"code\": null\n" +
                "                        }\n" +
                "                    },\n" +
                "                    \"saldoin\": \"363.71\",\n" +
                "                    \"setSum\": \"0\",\n" +
                "                    \"charge\": \"25.8\",\n" +
                "                    \"payment\": \"14.96\",\n" +
                "                    \"futurePays\": \"-4.91\",\n" +
                "                    \"saldoout\": \"337.91\",\n" +
                "                    \"forPaymentPast\": \"363.71\",\n" +
                "                    \"forPayment\": \"25.8\",\n" +
                "                    \"saldoInFine\": \"14.44\",\n" +
                "                    \"fineCalc\": null,\n" +
                "                    \"prepaysFine\": \"0\",\n" +
                "                    \"saldooutFine\": \"14.71\",\n" +
                "                    \"maket\": \"0\",\n" +
                "                    \"fine\": \".27\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"serviceInfo\": {\n" +
                "                        \"provider\": \"АО \\\"ЭНЕРГОСБЫТ ПЛЮС\\\"\",\n" +
                "                        \"service\": {\n" +
                "                            \"name\": \"КР НА СОИ (ЭЛ. ЭНЕРГИЯ)\",\n" +
                "                            \"code\": \"_10\"\n" +
                "                        },\n" +
                "                        \"mainCompany\": \"OOO \\\"ДОМОВОЙ\\\"\",\n" +
                "                        \"serviceGroup\": {\n" +
                "                            \"name\": null,\n" +
                "                            \"code\": null\n" +
                "                        }\n" +
                "                    },\n" +
                "                    \"saldoin\": \"423.77\",\n" +
                "                    \"setSum\": \"0\",\n" +
                "                    \"charge\": \"51.64\",\n" +
                "                    \"payment\": \"10.19\",\n" +
                "                    \"futurePays\": \"0\",\n" +
                "                    \"saldoout\": \"423.77\",\n" +
                "                    \"forPaymentPast\": \"423.77\",\n" +
                "                    \"forPayment\": \"0\",\n" +
                "                    \"saldoInFine\": \"34.39\",\n" +
                "                    \"fineCalc\": null,\n" +
                "                    \"prepaysFine\": \"0\",\n" +
                "                    \"saldooutFine\": \"34.39\",\n" +
                "                    \"maket\": \"-51.64\",\n" +
                "                    \"fine\": \"0\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"serviceInfo\": {\n" +
                "                        \"provider\": \"OOO \\\"ДОМОВОЙ\\\"\",\n" +
                "                        \"service\": {\n" +
                "                            \"name\": \"СОДЕРЖ. ДОМОВЛ.\",\n" +
                "                            \"code\": \"100\"\n" +
                "                        },\n" +
                "                        \"mainCompany\": \"OOO \\\"ДОМОВОЙ\\\"\",\n" +
                "                        \"serviceGroup\": {\n" +
                "                            \"name\": null,\n" +
                "                            \"code\": null\n" +
                "                        }\n" +
                "                    },\n" +
                "                    \"saldoin\": \"19411.55\",\n" +
                "                    \"setSum\": \"0\",\n" +
                "                    \"charge\": \"834.2\",\n" +
                "                    \"payment\": \"682.86\",\n" +
                "                    \"futurePays\": \"-414.8\",\n" +
                "                    \"saldoout\": \"18577.35\",\n" +
                "                    \"forPaymentPast\": \"19411.55\",\n" +
                "                    \"forPayment\": \"834.2\",\n" +
                "                    \"saldoInFine\": \"865.01\",\n" +
                "                    \"fineCalc\": null,\n" +
                "                    \"prepaysFine\": \"0\",\n" +
                "                    \"saldooutFine\": \"874.74\",\n" +
                "                    \"maket\": \"0\",\n" +
                "                    \"fine\": \"9.73\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"serviceInfo\": {\n" +
                "                        \"provider\": \"OOO \\\"ДОМОВОЙ\\\"\",\n" +
                "                        \"service\": {\n" +
                "                            \"name\": \"СОДЕРЖАНИЕ СПЕЦ.СЧЕТА\",\n" +
                "                            \"code\": \"104\"\n" +
                "                        },\n" +
                "                        \"mainCompany\": \"OOO \\\"ДОМОВОЙ\\\"\",\n" +
                "                        \"serviceGroup\": {\n" +
                "                            \"name\": null,\n" +
                "                            \"code\": null\n" +
                "                        }\n" +
                "                    },\n" +
                "                    \"saldoin\": \"735.59\",\n" +
                "                    \"setSum\": \"0\",\n" +
                "                    \"charge\": \"31.53\",\n" +
                "                    \"payment\": \"25.75\",\n" +
                "                    \"futurePays\": \"-15.68\",\n" +
                "                    \"saldoout\": \"704.06\",\n" +
                "                    \"forPaymentPast\": \"735.59\",\n" +
                "                    \"forPayment\": \"31.53\",\n" +
                "                    \"saldoInFine\": \"33.01\",\n" +
                "                    \"fineCalc\": null,\n" +
                "                    \"prepaysFine\": \"0\",\n" +
                "                    \"saldooutFine\": \"33.38\",\n" +
                "                    \"maket\": \"0\",\n" +
                "                    \"fine\": \".37\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"serviceInfo\": {\n" +
                "                        \"provider\": \"OOO \\\"ДОМОВОЙ\\\"\",\n" +
                "                        \"service\": {\n" +
                "                            \"name\": \"ОТОПЛЕНИЕ\",\n" +
                "                            \"code\": \"002\"\n" +
                "                        },\n" +
                "                        \"mainCompany\": \"OOO \\\"ДОМОВОЙ\\\"\",\n" +
                "                        \"serviceGroup\": {\n" +
                "                            \"name\": null,\n" +
                "                            \"code\": null\n" +
                "                        }\n" +
                "                    },\n" +
                "                    \"saldoin\": \"0\",\n" +
                "                    \"setSum\": \"0\",\n" +
                "                    \"charge\": \"0\",\n" +
                "                    \"payment\": \"0\",\n" +
                "                    \"futurePays\": \"0\",\n" +
                "                    \"saldoout\": \"0\",\n" +
                "                    \"forPaymentPast\": \"0\",\n" +
                "                    \"forPayment\": \"0\",\n" +
                "                    \"saldoInFine\": \"0\",\n" +
                "                    \"fineCalc\": null,\n" +
                "                    \"prepaysFine\": \"0\",\n" +
                "                    \"saldooutFine\": \"0\",\n" +
                "                    \"maket\": \"0\",\n" +
                "                    \"fine\": \"0\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"serviceInfo\": {\n" +
                "                        \"provider\": \"ООО \\\"ГУБАХИНСКАЯ ЭНЕРГЕТИЧЕСКАЯ КОМПАНИЯ\\\"\",\n" +
                "                        \"service\": {\n" +
                "                            \"name\": \"ОТОПЛЕНИЕ\",\n" +
                "                            \"code\": \"002\"\n" +
                "                        },\n" +
                "                        \"mainCompany\": \"OOO \\\"ДОМОВОЙ\\\"\",\n" +
                "                        \"serviceGroup\": {\n" +
                "                            \"name\": null,\n" +
                "                            \"code\": null\n" +
                "                        }\n" +
                "                    },\n" +
                "                    \"saldoin\": \"0\",\n" +
                "                    \"setSum\": \"0\",\n" +
                "                    \"charge\": \"0\",\n" +
                "                    \"payment\": \"0\",\n" +
                "                    \"futurePays\": \"0\",\n" +
                "                    \"saldoout\": \"0\",\n" +
                "                    \"forPaymentPast\": \"0\",\n" +
                "                    \"forPayment\": \"0\",\n" +
                "                    \"saldoInFine\": \"0\",\n" +
                "                    \"fineCalc\": null,\n" +
                "                    \"prepaysFine\": \"0\",\n" +
                "                    \"saldooutFine\": \"0\",\n" +
                "                    \"maket\": \"0\",\n" +
                "                    \"fine\": \"0\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"serviceInfo\": {\n" +
                "                        \"provider\": \"ООО \\\"СПЕЦАВТОХОЗЯЙСТВО\\\"\",\n" +
                "                        \"service\": {\n" +
                "                            \"name\": \"ОБРАЩЕНИЕ С ТКО\",\n" +
                "                            \"code\": \"508\"\n" +
                "                        },\n" +
                "                        \"mainCompany\": \"OOO \\\"ДОМОВОЙ\\\"\",\n" +
                "                        \"serviceGroup\": {\n" +
                "                            \"name\": null,\n" +
                "                            \"code\": null\n" +
                "                        }\n" +
                "                    },\n" +
                "                    \"saldoin\": \"1370.33\",\n" +
                "                    \"setSum\": \"0\",\n" +
                "                    \"charge\": \"0\",\n" +
                "                    \"payment\": \"29.55\",\n" +
                "                    \"futurePays\": \"0\",\n" +
                "                    \"saldoout\": \"1370.33\",\n" +
                "                    \"forPaymentPast\": \"1370.33\",\n" +
                "                    \"forPayment\": \"0\",\n" +
                "                    \"saldoInFine\": \"0\",\n" +
                "                    \"fineCalc\": null,\n" +
                "                    \"prepaysFine\": \"0\",\n" +
                "                    \"saldooutFine\": \"0\",\n" +
                "                    \"maket\": \"0\",\n" +
                "                    \"fine\": \"0\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"serviceInfo\": {\n" +
                "                        \"provider\": \"ООО \\\"ГУБАХИНСКАЯ ЭНЕРГЕТИЧЕСКАЯ КОМПАНИЯ\\\"\",\n" +
                "                        \"service\": {\n" +
                "                            \"name\": \"ГОР. ВОДА (ПОВЫШ.КОЭФ-Т)\",\n" +
                "                            \"code\": \"04_\"\n" +
                "                        },\n" +
                "                        \"mainCompany\": \"OOO \\\"ДОМОВОЙ\\\"\",\n" +
                "                        \"serviceGroup\": {\n" +
                "                            \"name\": null,\n" +
                "                            \"code\": null\n" +
                "                        }\n" +
                "                    },\n" +
                "                    \"saldoin\": \"0\",\n" +
                "                    \"setSum\": \"0\",\n" +
                "                    \"charge\": \"0\",\n" +
                "                    \"payment\": \"0\",\n" +
                "                    \"futurePays\": \"0\",\n" +
                "                    \"saldoout\": \"0\",\n" +
                "                    \"forPaymentPast\": \"0\",\n" +
                "                    \"forPayment\": \"0\",\n" +
                "                    \"saldoInFine\": \"0\",\n" +
                "                    \"fineCalc\": null,\n" +
                "                    \"prepaysFine\": \"0\",\n" +
                "                    \"saldooutFine\": \"0\",\n" +
                "                    \"maket\": \"0\",\n" +
                "                    \"fine\": \"0\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"serviceInfo\": {\n" +
                "                        \"provider\": \"АО \\\"ЭНЕРГОСБЫТ ПЛЮС\\\"\",\n" +
                "                        \"service\": {\n" +
                "                            \"name\": \"ЭЛЕКТРОЭНЕРГИЯ (ПОВЫШ.КОЭФ-Т)\",\n" +
                "                            \"code\": \"10_\"\n" +
                "                        },\n" +
                "                        \"mainCompany\": \"АО \\\"ЭНЕРГОСБЫТ ПЛЮС\\\"\",\n" +
                "                        \"serviceGroup\": {\n" +
                "                            \"name\": null,\n" +
                "                            \"code\": null\n" +
                "                        }\n" +
                "                    },\n" +
                "                    \"saldoin\": \"0\",\n" +
                "                    \"setSum\": \"0\",\n" +
                "                    \"charge\": \"0\",\n" +
                "                    \"payment\": \"0\",\n" +
                "                    \"futurePays\": \"0\",\n" +
                "                    \"saldoout\": \"0\",\n" +
                "                    \"forPaymentPast\": \"0\",\n" +
                "                    \"forPayment\": \"0\",\n" +
                "                    \"saldoInFine\": \"0\",\n" +
                "                    \"fineCalc\": null,\n" +
                "                    \"prepaysFine\": \"0\",\n" +
                "                    \"saldooutFine\": \"0\",\n" +
                "                    \"maket\": \"0\",\n" +
                "                    \"fine\": \"0\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"serviceInfo\": {\n" +
                "                        \"provider\": \"МУП Г. САРАПУЛА \\\"САРАПУЛЬСКИЙ ВОДОКАНАЛ\\\"\",\n" +
                "                        \"service\": {\n" +
                "                            \"name\": \"КР НА СОИ (ВОДОСНАБЖЕНИЕ)\",\n" +
                "                            \"code\": \"103_\"\n" +
                "                        },\n" +
                "                        \"mainCompany\": \"OOO \\\"ДОМОВОЙ\\\"\",\n" +
                "                        \"serviceGroup\": {\n" +
                "                            \"name\": \"КР НА СОИ (ВОДОСНАБЖЕНИЕ)\",\n" +
                "                            \"code\": \"17\"\n" +
                "                        }\n" +
                "                    },\n" +
                "                    \"saldoin\": \"243.31\",\n" +
                "                    \"setSum\": \"-20.00\",\n" +
                "                    \"charge\": \"21.59\",\n" +
                "                    \"payment\": \"13.77\",\n" +
                "                    \"futurePays\": \"-2.38\",\n" +
                "                    \"saldoout\": \"221.72\",\n" +
                "                    \"forPaymentPast\": \"243.31\",\n" +
                "                    \"forPayment\": \"21.59\",\n" +
                "                    \"saldoInFine\": \"10.62\",\n" +
                "                    \"fineCalc\": null,\n" +
                "                    \"prepaysFine\": \"0\",\n" +
                "                    \"saldooutFine\": \"10.85\",\n" +
                "                    \"maket\": \"0\",\n" +
                "                    \"fine\": \".23\"\n" +
                "                }\n" +
                "            ],\n" +
                "            \"account\": \"0229077238\"\n" +
                "        }\n" +
                "    ]\n" +
                "}";
    }

    //@Test
    public void setReceiptsFromCnToAppForId_house() {

        params = new Object[]{"4798", myDate.getPreviousMonthLastDate(), "Сарапул, 20 Лет Победы, д.11"};

        assertDoesNotThrow(() -> new InvokerProtectedMethod()
                .invokeProtectedMethod(sinkService, "setReceiptsFromCnToAppForId_house", params));

    }

    @Test
    public void sendMail() {
        params = new Object[]{"Тест", "Тестовая строка"};
        assertDoesNotThrow(() -> new InvokerProtectedMethod()
                .invokeProtectedMethod(sinkService, "sendMail", params));

    }

    @Test
    public void getCnHouseList() throws Exception {
        assertDoesNotThrow(() -> sinkService.getCnHouseList());
    }

    @Test
    public void deleteAddressHouseCnEntitiesByToken() throws Exception {
        params = new Object[]{tokenFactory.getToken()};
        assertDoesNotThrow(() -> new InvokerProtectedMethod()
                .invokeProtectedMethod(sinkService, "deleteAddressHouseCnEntitiesByToken", params));
    }
}