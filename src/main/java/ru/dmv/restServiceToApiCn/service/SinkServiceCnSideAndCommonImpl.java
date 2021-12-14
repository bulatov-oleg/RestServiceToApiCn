package ru.dmv.restServiceToApiCn.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.stereotype.Service;
import ru.dmv.restServiceToApiCn.dto.*;
import ru.dmv.restServiceToApiCn.exception.RestServiceToApiCnException;
import ru.dmv.restServiceToApiCn.exception.RestServiceToApiCnTransactionException;
import ru.dmv.restServiceToApiCn.model.*;
import ru.dmv.restServiceToApiCn.repository.*;
import ru.dmv.cnNovosibirsk.dto.AllCounters.*;
import ru.dmv.cnNovosibirsk.dto.BalanceDetail.AccBalance;
import ru.dmv.cnNovosibirsk.dto.BalanceDetail.Balance;
import ru.dmv.cnNovosibirsk.dto.BalanceDetail.BalanceDetailRequest;
import ru.dmv.cnNovosibirsk.dto.BalanceDetail.BalanceDetailResponse;
import ru.dmv.cnNovosibirsk.dto.CntCheck.*;
import ru.dmv.cnNovosibirsk.dto.HouseList.AddressHouseDto;
import ru.dmv.cnNovosibirsk.dto.HouseList.CnHouseListRequestDto;
import ru.dmv.cnNovosibirsk.dto.HouseList.CnHouseListResponceDto;
import ru.dmv.cnNovosibirsk.dto.HouseList.HouseList;
import ru.dmv.cnNovosibirsk.dto.Pays.PaysDocument;
import ru.dmv.cnNovosibirsk.dto.Pays.PaysRequest;
import ru.dmv.cnNovosibirsk.dto.Pays.PaysResponse;
import ru.dmv.cnNovosibirsk.factory.CnGetTokenFactory;
import ru.dmv.cnNovosibirsk.service.CnApiServiceImpl;
import ru.dmv.lk.factories.ObjectMapperFactory;
import ru.dmv.lk.service.EmailService;
import ru.dmv.lk.utils.MyDate;
import ru.dmv.lk.utils.MyUtil;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@SuppressWarnings("ALL")
@Service
@Scope("prototype") //при каждом new будет создан новый экземпляр класса
public class SinkServiceCnSideAndCommonImpl implements SinkServiceCnSideAndCommon {

    private final String SEPARATOR = System.lineSeparator();
    private final Logger log = LoggerFactory.getLogger(SinkServiceCnSideAndCommonImpl.class);
    private final String CN_ALLCOUNTERS = "/AllCounters";
    private final String CN_CNTCHECK = "/CntCheck ";
    private final String CN_HOUSELIST = "/HouseList";
    private final String CN_ECHO = "/Echo";
    private final String CN_BALANCEDETAIL = "/BalanceDetail";
    private final String CN_PAYS = "/Pays";
    private final String APP_IMPORT_VALUES = "/exchange/import/values";
    private final String APP_GETCITY = "/api/get-city";
    private final String APP_RECEIPTS = "/exchange/import/receipts";


    //потоконезависимо, т.к. @Scope("prototype")
    String whatDo = "all"; //all,for, without услуги из serviceList
    ServiceListDto serviceList = null;
    StringBuilder resultsInfo;

    @Value("${app.app.url}")
    private String APP_URL;
    @Value("${app.cn.url}")
    private String CN_URL;  //  "https://www.kvartplata.ru:4433";
    @Value("${app.app.limitSizeOfPackage}")
    private int APP_LIMIT_SIZE_OF_PACKAGE;
    @Value("${app.app.AppTimeout}")
    private int APP_REQUEST_TIMEOUT;
    @Value("${app.cn.limitSizeOfPackage}")
    private long CN_LIMIT_SIZE_OF_PACKAGE;
    @Value("${app.mail.sendToEmail}")
    private String SEND_TO_EMAIL;
    @Value("${app.mail.email.subject}")
    private String EMAIL_SUBJECT; //придет в StandardCharsets.ISO_8859_1
    @Value("${app.getAppValues.onlyNew}")
    private int GET_APP_ALL_VALUES; // 0 берет последние значения без учета пользователь их ввел В МП или импортированы были, 1 - только пользовательские будет отдавать.

    private String token; //токен получаем у сопровождения ЦН - доступ к определенным данным
    private ObjectMapperFactory objectMapper;
    private EmailService emailService;
    private MyDate myDate;
    private MyUtil myUtil;
    private CnApiServiceImpl cnApiService;
    private AppApiServiceImpl appApiService;
    private SinkServiceAppSideImpl serviceAppSide;
    private ObjectFactory<SinkServiceCnSideAndCommonImpl> objectFactory;
    private CnAddressHouseRepo cnAddressHouseRepo;
    private CnMeterRepo cnMeterRepo;
    private CnMeterValuesRepo cnMeterValuesRepo;
    private AppHouseRepo appHouseRepo;
    private AppAddressLsRepo appAddressLsRepo;
    private AppMetersValuesRepo appMetersValuesRepo;
    private CnAddressLsRepo cnAddressLsRepo;



    @Autowired
    public SinkServiceCnSideAndCommonImpl(CnGetTokenFactory tokenFactory,
                                          CnAddressHouseRepo cnAddressHouseRepo,
                                          CnMeterRepo cnMeterRepo,
                                          CnMeterValuesRepo cnMeterValuesRepo,
                                          AppHouseRepo appHouseRepo,
                                          AppAddressLsRepo appAddressLsRepo,
                                          AppMetersValuesRepo appMetersValuesRepo,
                                          CnAddressLsRepo cnAddressLsRepo,
                                          ObjectMapperFactory objectMapperFactory,
                                          MyDate myDate,
                                          MyUtil myUtil,
                                          @Qualifier("main") EmailService emailService,
                                          CnApiServiceImpl cnApiService,
                                          SinkServiceAppSideImpl serviceAppSide,
                                          ObjectFactory<SinkServiceCnSideAndCommonImpl> objectFactory,
                                          AppApiServiceImpl appApiService) throws Exception {
        //this.tokenFactory = tokenFactory;
        this.myDate = myDate;
        this.myUtil = myUtil;
        this.token = tokenFactory.getToken();
        this.objectMapper = objectMapperFactory;
        this.emailService = emailService;
        this.cnApiService = cnApiService;
        this.appApiService = appApiService;
        this.serviceAppSide = serviceAppSide;
        this.objectFactory = objectFactory;
        this.cnAddressHouseRepo = cnAddressHouseRepo;
        this.cnMeterRepo = cnMeterRepo;
        this.cnMeterValuesRepo = cnMeterValuesRepo;
        this.appHouseRepo = appHouseRepo;
        this.appAddressLsRepo = appAddressLsRepo;
        this.appMetersValuesRepo = appMetersValuesRepo;
        this.cnAddressLsRepo = cnAddressLsRepo;
    }


    @Override
    public SinkServiceCnSideAndCommon getInstance() {
        return objectFactory.getObject();
    }

    @Override
    public String test() throws Exception {
        log.info("Проверяем связь с ЦН");
        String result;
        try {
            return getResultTestConnectCnAndApp();
        } catch (Exception e) {
            throw new RestServiceToApiCnException("Тест связи с api не прошел по " + APP_URL + " и " +
                    CN_URL + ". " + e.getMessage() + " " + e.getCause(), e);
        }
    }

    private String getResultTestConnectCnAndApp() throws Exception {

        //Здесь безразлично, что вернет запрос. Если вернет body , то все хорошо
        String rBodyApp = appApiService.getBodyResponseApp(APP_URL + APP_GETCITY, "");

        //
        String url = CN_URL + CN_ECHO;
        String rBodyCn = cnApiService.getBodyResponseCn(url, "{\"Message\": \"test\"}");
        if (rBodyCn.contains("\"Message\":\"test\""))
            return "Ok. Связь с api ЦН и МП по " + APP_URL + " и " + CN_URL;
        else
            return "Error. Проблемы связи с Api  по " + APP_URL + " и " + CN_URL;
    }


    /**
     * Запишет в ЦН данные из МП таблицы app_meters_value
     *
     * @param whatDo      "all" - запишет все услуги
     *                    "for" - запишет только услуги из списка кодов услуг по ЦН serviceList
     *                    "without" - запишет услуги, не включенные в список по ЦН serviceList
     *                    Проверек параметра не производим, поскольку вход только из соответствующих запросов
     * @param serviceList - список кодов услуг по ЦН, для "all" не рассматривается, должен быть null
     *                    для "for" и "without" проверяем на null и пустоту
     * @return "" при успехе без ошибок, иначе информацию от ЦН о каждом ИПУ для каждого пакета
     * @throws Exception SinkException если что-то не так или что-то другое
     */
    @Override
    public String setCnValues() throws Exception {

        resultsInfo = new StringBuilder();

        long limitAccountsForRequest = CN_LIMIT_SIZE_OF_PACKAGE;
        CntCheckRequest cntCheckRequest;

        logResultInfo("Передаем в ЦН: показания.");

        // Получим записи с лиц.счетами у которых есть показания из МП
        logResultInfo("Начали выборку показаний МП из таблицы БД");
        List<AppMeterValuesEntity> appMeterValuesEntities = appMetersValuesRepo.findAllByToken(token);
        if (!checkRecordsAppMeterValues(appMeterValuesEntities)) {
            return "Error. Нет данных о показаниях МП. Выполните /apiApp/getAppValues";
        }

        //добавим Account к каждой записи с показниями в List<AppValuesWithPa>
        List<AppValuesWithPa> appValuesWithPas = addPaTo(appMeterValuesEntities);

        //выделим только те показания, которые затребованы для передачи через whatDo и serviceList
        List<AppValuesWithPa> appValuesWithPaRequestedList = appValuesRequested(appValuesWithPas);

        logResultInfo("Закончили выборку показаний  МП." + SEPARATOR +
                "Всего выбрано для передачи " + appValuesWithPaRequestedList.size());

        // получим список уникальных лицевых счетов, по которым пришли показания из МП
        List<String> accountsAll = getListOfUniquePa(appValuesWithPaRequestedList);

        logResultInfo("Всего выбрано " + accountsAll.size() + " лицевых счетов для передачи");

        int countRequests = 0;

        while (true) { //в каждом request не более limitAccountsForRequest лицевых счетов
            List<String> accounts = accountsAll.stream()
                    .skip(countRequests * limitAccountsForRequest)
                    .limit(limitAccountsForRequest)
                    .collect(Collectors.toList());

            if (accounts.size() == 0) break; //выходим из цикла

            //отправлем запрос
            cntCheckRequest = new CntCheckRequest();
            cntCheckRequest.setVerificationList(getVerificationList(accounts, appValuesWithPaRequestedList));
            cntCheckRequest.setToken(token);
            requestCntCheck(cntCheckRequest);   //собственно запрос. Exception не ожидается, запишет в лог и результат
            // информацию о выполнении

            countRequests++;

            if (accounts.size() < limitAccountsForRequest)
                break; //выходим из цикла


        }
        logResultInfo(" Закончили передачу в ЦН.");
        return resultsInfo.toString();
    }

    private List<Verification> getVerificationList(List<String> accounts,
                                                   List<AppValuesWithPa> appValuesWithPaRequestedList)
            throws Exception {
        List<Verification> verificationList = new ArrayList<>();
        Verification verification;
        //создаем Verification для каждого лицевого счета, для которого есть показания в МП
        //Учитывая что метод может быть вызван только для набора услуг,
        // формируем Verification в зависимости от услуг с кодом из ЦН
        for (String account : accounts) {
            verification = new Verification();
            verification.setAccount(account);
            verification.setVerificationInfo(getVerificationInfos(account, appValuesWithPaRequestedList));
            verificationList.add(verification);
        }

        return verificationList;
    }

    //выделим только те показания, которые затребованы для передачи через whatDo и serviceList
    private List<AppValuesWithPa> appValuesRequested(List<AppValuesWithPa> appValuesWithPas)
            throws Exception {
        List<AppValuesWithPa> appValuesWithPaRequestedList = appValuesWithPas;
        switch (whatDo) {
            case "all": {
                logResultInfo("Выбираем показания по всем услугам ");
                break;
            }
            case "for": {
                logResultInfo("Выбираем показния по услугам: (коды услуг по ЦН) " + serviceList.getServiceList());
                appValuesWithPaRequestedList = appValuesWithPas.stream()
                        .filter(a -> serviceList.getServiceList().contains(a.getCodeService()))
                        .collect(Collectors.toList());
                break;
            }
            case "without": {
                logResultInfo("Выбираем показания по всем услугам, кроме: (коды услуг по ЦН) " + serviceList.getServiceList());
                appValuesWithPaRequestedList = appValuesWithPas.stream()
                        .filter(a -> !serviceList.getServiceList().contains(a.getCodeService()))
                        .collect(Collectors.toList());
                break;
            }
            default:
                throw new RestServiceToApiCnException("Параметр whatDo = " + whatDo + " не обрабатывается", null);
        }
        return appValuesWithPaRequestedList;
    }

    private void logResultInfo(String message) {
        log.info(message);
        resultsInfo.append(message).append(SEPARATOR);
    }

    //Добавим pa (ассount) к показаниям ИПУ МП
    private List<AppValuesWithPa> addPaTo(List<AppMeterValuesEntity> appMeterValuesEntities) {
        //account получим из данных об ИПУ, полученных из ЦН
        //Добавим также код услуги и наименование услуги по ЦН
        List<AppValuesWithPa> appValuesWithPas = new ArrayList<>();
        CnMeterEntity cnMeterEntity;
        List<CnMeterEntity> cnMeterEntities = cnMeterRepo.findAllByToken(token);
        for (AppMeterValuesEntity a : appMeterValuesEntities) {
            cnMeterEntity = cnMeterEntities.stream()
                    .filter(entity -> entity.getDeviceId().equals(a.getDevice_id()))
                    .findFirst()
                    .orElse(null);
            if (cnMeterEntity != null)
                appValuesWithPas.add(new AppValuesWithPa(a.getDevice_id(),
                        a.getValue(),
                        a.getValue_night(),
                        cnMeterEntity.getPa(),
                        cnMeterEntity.getScales(),
                        cnMeterEntity.getCodeService(),
                        cnMeterEntity.getNameService()));

        }

        return appValuesWithPas;
    }

    private boolean checkRecordsAppMeterValues(List<AppMeterValuesEntity> appMeterValuesEntities) {
        if (appMeterValuesEntities == null) return false;
        if (appMeterValuesEntities.size() == 0) return false;
        return true;
    }

    // получим список уникальных лицевых счетов, по которым пришли показания из МП
    private List<String> getListOfUniquePa(List<AppValuesWithPa> appValuesWithPaRequestedList) {
        return appValuesWithPaRequestedList.stream()
                .map(c -> c.getPa())
                .distinct()
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());
    }

    //собственно запрос CntCheck
    private boolean requestCntCheck(CntCheckRequest cntCheckRequest) {

        String url = CN_URL + CN_CNTCHECK;
        String body;
        try {
            body = objectMapper.writeValueAsString(cntCheckRequest);
        } catch (Exception e) {
            logResultInfo("Неожиданный Exception преобразования в json для запроса " +
                    url + " с body " + cntCheckRequest + " " + e.getMessage() + " " + e.getCause() + SEPARATOR);
            return false;
        }

        //Возможен Exception при выполнении запросов
        String rBody;
        try {
            rBody = cnApiService.getBodyResponseCn(url, body);
        } catch (Exception e) {
            logResultInfo("Для " + cntCheckRequest + SEPARATOR +
                    e.getMessage() + " " + e.getCause() + SEPARATOR);
            return false;
        }

        CntCheckResponce cntCheckResponce;
        try {
            cntCheckResponce = objectMapper.readValue(rBody, CntCheckResponce.class);
        } catch (Exception e) {
            logResultInfo("Для " + cntCheckRequest + SEPARATOR +
                    e.getMessage() + " " + e.getCause() + SEPARATOR);
            return false;
        }

        if (cntCheckResponce == null) {
            logResultInfo(url + " Получен null в данных для " + cntCheckRequest);
            return false;
        }

        if (cntCheckResponce.getAnswer().getMessage() != null) {
            logResultInfo(url + " " + " Ошибки об ИПУ, полученные с сервера " + SEPARATOR);
            logResultInfo(" Сообщение общее - " + cntCheckResponce.getAnswer().toString() + SEPARATOR);
            logResultInfo(" Сообщения о каждом ИПУ - " + SEPARATOR);
            CnMeterEntity cnMeterEntity;
            for (CCResult result : cntCheckResponce.getAnsResultList()) {
                //получаем лиц.счет по ID ипу для указания по какому лиц. счету произошла ошибка.
                cnMeterEntity = cnMeterRepo.findByTokenAndDeviceId(token, result.getID());
                if (cnMeterEntity != null) {
                    //Записываем ошибку по ID для указанного выше лиц. счета
                    logResultInfo("Для л.с. " + cnMeterEntity.getPa() +
                            " и ID ИПУ " + result.getID() +
                            ": " + result.toString() + SEPARATOR);
                }
            }
        }
        return true;
    }

    //сформируем для ЦН для одного лицевого счета
    private List<VerificationInfo> getVerificationInfos(String account,
                                                        List<AppValuesWithPa> appValuesWithPas)
            throws Exception {
        List<VerificationInfo> verificationInfos = new ArrayList<>();
        VerificationInfo verificationInfo;
        //Список показаний для лицевого счета
        List<AppValuesWithPa> appValuesForAccount = appValuesWithPas.stream()
                .filter(a -> a.getPa().equals(account))
                .sorted(Comparator.comparing(AppValuesWithPa::getDevice_id))
                .collect(Collectors.toList());
        //Список уникальных ИПУ на лицевом
        //noinspection SimplifyStreamApiCallChains
        List<String> uniqueDeviceIdList = appValuesForAccount.stream()
                .map(c -> c.getDevice_id())
                .distinct()
                .collect(Collectors.toList());
        for (String device_id : uniqueDeviceIdList) {
            verificationInfo = new VerificationInfo();
            verificationInfo.setID(device_id);
            verificationInfo.setIndicationDate(myDate.today()); //Это в целом неверно, поскольку нужна дата ввода пользователем в МП, а ее пока нет в ответе);
            verificationInfo.setIndications(getIndications(device_id, appValuesForAccount));
            if (verificationInfo.getIndications() != null) //может быть, если прибор не нужно передавать См. whatDo
                verificationInfos.add(verificationInfo);
        }
        return verificationInfos;
    }

    //сформируем показания для одного устройства
    private List<Indication> getIndications(String device_id,
                                            List<AppValuesWithPa> appValuesForAccount)
            throws Exception {
        List<Indication> indications = new ArrayList<>();
        List<AppValuesWithPa> valuesAll = appValuesForAccount.stream()
                .filter(a -> a.getDevice_id().equals(device_id))
                .collect(Collectors.toList());

        // для whatDo "all" - берем все услуги
        //            "for" - берем услуги из списка serviceList
        //            "without" - берем услуги НЕ ИЗ списка serviceList
        List<AppValuesWithPa> values = getRequestedValuesForDevice(valuesAll);

        if (values.size() == 0) {
            return null; //Нет ИПУ, который нужно передавать См. whatDo
        }
        for (AppValuesWithPa indicationValue : values) {
            appendToIndicationsForCn(indications, indicationValue);
        }
        return indications;
    }

    private void appendToIndicationsForCn(List<Indication> indications,
                                          AppValuesWithPa indicationValue)
            throws Exception {
        Indication indication = new Indication();
        //Здесь предусмотрена только работа по простым счетчикам в 1 или 2 зоны
        double value = indicationValue.getValue();
        double valueNight = indicationValue.getValueNight();
        if (value != 0 && valueNight == 0) {
            //показания дневные, поместить на шкалу 1 в ЦН
            indication.setScale("1");
            indication.setIndication(String.valueOf(value));
            indications.add(indication);
        } else if (value == 0 && valueNight != 0) {
            //показания дневные, поместить на шкалу 2 в ЦН
            indication.setScale("2");
            indication.setIndication(String.valueOf(valueNight));
            indications.add(indication);
        } else if (value == 0 && valueNight == 0 && indicationValue.getScales() == 1) {  // Случай редкий. Рассматриваем как поданные 0 дневные
            //Если пришли 0, а счетчик однозонный
            // показания дневные, поместить на шкалу 1 в ЦН
            indication.setScale("1");
            indication.setIndication(String.valueOf(value));
            indications.add(indication);
        } else if (value == 0 && valueNight == 0 && indicationValue.getScales() == 2) {  // Случай редкий. Рассматриваем как поданные 0 дневные и ночные
            //Если пришли 0, а счетчик двузонный
            // показания дневные, поместить на шкалу 1 в ЦН
            indication.setScale("1");
            indication.setIndication(String.valueOf(value));
            indications.add(indication);
            //показания дневные, поместить на шкалу 1 в ЦН
            indication = new Indication();
            indication.setScale("2");
            indication.setIndication(String.valueOf(value));
            indications.add(indication);
        } else
            throw new RestServiceToApiCnException("Для показаний " + indicationValue + " нет обработки для передачи в ЦН", null);
    }

    private List<AppValuesWithPa> getRequestedValuesForDevice(List<AppValuesWithPa> valuesAll)
            throws Exception {
        List<AppValuesWithPa> values = valuesAll;
        switch (whatDo) {
            case "all": {
                values = valuesAll;
                break;
            }
            case "for": {
                values = valuesAll.stream()
                        .filter(a -> serviceList.getServiceList().contains(a.getCodeService()))
                        .collect(Collectors.toList());
                break;
            }
            case "without": {
                values = valuesAll.stream()
                        .filter(a -> !serviceList.getServiceList().contains(a.getCodeService()))
                        .collect(Collectors.toList());
                break;
            }
            default:
                throw new RestServiceToApiCnException("Параметр whatDo = " + whatDo + " не поддерживается.", null);
        }
        return values;
    }

    /**
     * Установит соответствие idHouse App и ЦН fias в таблице cn_house
     *
     * @return строку со списоком домов в ЦН, для которых не удалось установить соответствие
     */
    @Override
    public String setSinkIdHouseAppAndCn() throws Exception {
        boolean isProblems = false;
        resultsInfo = new StringBuilder();
        logResultInfo("Устанавливаем соответствие домов ЦН и МП.");

        ;
        //Адреса полученные из ЦН
        List<CnAddressHouseEntity> cnAddressHouseEntities = getAddressHouses();
        for (CnAddressHouseEntity cnAddressHouseEntity : cnAddressHouseEntities) {

            checkInfoInCnAddressHouse(cnAddressHouseEntity);

            //Для каждого адреса из ЦН ищем соответствие в адресах из App, используя формат адреса для App
            AppHouseEntity appHouseEntity = getForAddressForAppFromCn(cnAddressHouseEntity);

            if (appHouseEntity.getToken() == null) { // будет в случае неудачного поиска выше
                logResultInfo("Для адреса в CN " + cnAddressHouseEntity.getAddressForApp() +
                        "не найдено соответстиве в App.");
                continue;
            }
            //в полученных из ЦН данных устанавливаем id адреса, полученного из App
            cnAddressHouseEntity.setAppIdHouse(appHouseEntity.getId_house());
            saveCnAddressHouseEntity(cnAddressHouseEntity);
        }
        if (isProblems) {
            logResultInfo("Часть адресов домов из ЦН присутствуют в App.");
            return "Нужно внести адреса в МП";
        } else {
            logResultInfo("Ok. Все адреса домов из ЦН присутствуют в App.");
            return "Ok. Установлено соответствие домов ЦН и МП ";
        }
    }

    //@Transactional
    public void saveCnAddressHouseEntity(CnAddressHouseEntity cnAddressHouseEntity) throws RestServiceToApiCnTransactionException {
        try {
            cnAddressHouseRepo.saveAndFlush(cnAddressHouseEntity);
        } catch (RuntimeException e) {
            throw new RestServiceToApiCnTransactionException(cnAddressHouseEntity, e);
        }

    }

    private void checkInfoInCnAddressHouse(CnAddressHouseEntity cnAddressHouseEntity) {
        if (cnAddressHouseEntity.getFias() == null || cnAddressHouseEntity.getFias().equals("")) {
            logResultInfo("Для адреса в CN " + cnAddressHouseEntity.getAddressForApp() +
                    " fias равен null или пусто");
        }
        if (cnAddressHouseEntity.getAddressForApp() == null) {
            logResultInfo("Для адреса в CN" +
                    cnAddressHouseEntity.getCity() + ", " +
                    cnAddressHouseEntity.getStreet() + ", " +
                    cnAddressHouseEntity.getHouse() + ", " +
                    cnAddressHouseEntity.getWing() +
                    " полный адрес для App равен null");
        }
    }

    private AppHouseEntity getForAddressForAppFromCn(CnAddressHouseEntity cnAddressHouseEntity)
            throws Exception {
        AppHouseEntity appHouseEntity;
        try {
            appHouseEntity = appHouseRepo.findByTokenAndAddress(token, cnAddressHouseEntity.getAddressForApp());
        } catch (IncorrectResultSizeDataAccessException e) {
            e.printStackTrace();
            throw new RestServiceToApiCnException("Обнаружена двойная запись о доме для App " +
                    "token: " + token +
                    ", адрес: " + cnAddressHouseEntity.getAddressForApp() + ". " +
                    e.getMessage(), e);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RestServiceToApiCnException("Ошибка при поиске дома для App " +
                    "token: " + token +
                    ", адрес: " + cnAddressHouseEntity.getAddressForApp() + ". " +
                    e.getMessage(), e);
        }
        if (appHouseEntity == null) {
            appHouseEntity = new AppHouseEntity();
        }
        return appHouseEntity;
    }

    /**
     * Получить из ЦН список домов и положить в табл cn_house
     *
     * @throws Exception что-то не так
     */
    @Override

    public String getCnHouseList() throws Exception {
        log.info("Принимаем из ЦН: Дома.");

        //String WELCOME_URL ="https://www.kvartplata.ru:4433/HouseList";
        deleteAddressHouseCnEntitiesByToken(token);
        CnAddressHouseEntity addressHouse;
        CnHouseListResponceDto cnHouseListResponceDto = getCnHouseListResponce();
        List<HouseList> houseLists = cnHouseListResponceDto.getHouseList();
        for (HouseList houseList : houseLists) {
            for (AddressHouseDto addressHouseDto : houseList.getAddress()) {
                addressHouse = new CnAddressHouseEntity();
                addressHouse.setToken(token);
                addressHouse.setCity(addressHouseDto.getCity());
                addressHouse.setStreet(addressHouseDto.getStreet());
                addressHouse.setHouse(addressHouseDto.getHouse());
                addressHouse.setFias(addressHouseDto.getFias());
                addressHouse.setFullAddr(addressHouse.getAddressForApp());
                addressHouse.setIdHouse(houseList.getIdHouse());
                addressHouse.setAppIdHouse(null); //заполним для согласования позже
                saveAddressHouse(addressHouse);
            }
        }
        log.info("Закончили прием из ЦН: Дома.");
        return "Ок. Дома из ЦН приняты";
    }


    //@Transactional
    public void deleteAddressHouseCnEntitiesByToken(String token) throws Exception {
        try {
            cnAddressHouseRepo.deleteAddressHouseCnEntitiesByToken(token);
        } catch (RuntimeException e) {
            throw new RestServiceToApiCnTransactionException("Не выполнен cnAddressHouseRepo.deleteAddressHouseCnEntitiesByToken(token) ", e);
        }
    }


    //@Transactional
    public void saveAddressHouse(CnAddressHouseEntity addressHouse) throws RestServiceToApiCnTransactionException {
        try {
            cnAddressHouseRepo.saveAndFlush(addressHouse);
        } catch (RuntimeException e) {
            throw new RestServiceToApiCnTransactionException(addressHouse, e);
        }
    }

    /**
     * Получает из ЦН данные о лицевых счетах, адресах, ИПУ и показаниях
     * записывает в соответствующие таблицы БД
     *
     * @throws Exception RestServiceToApiCnException
     */
    @Override

    public String getCnLsAndCounters() throws Exception {
        resultsInfo = new StringBuilder();
        logResultInfo("Принимаем из ЦН: Лицевые счета, ИПУ, показания.");

        List<CnAddressHouseEntity> addressHouses = getAddressHouses();


        cnAddressLsRepoDeleteAllByToken(token); //чистим инфрмацию о всех лиц. счетах из ЦН
        cnMeterRepoDeleteAllByToken(token); // Удаляем все ИПУ
        cnMeterValuesRepoDeleteAllByToken(token); // удаляем все показания

        for (CnAddressHouseEntity addressHouse : addressHouses) {

            logResultInfo("Обрабатываем " + addressHouse.getAddressForApp());

            if (addressHouse.getFias() == null || addressHouse.getFias().equals("")) {
                logResultInfo(" Данные лиц.счетов и ИПУ не получены. fias равен null или пусто");
                continue;
            }

            //Запрос в ЦН на ИПУ по fias дома, придет объект или Exception
            AllCountersResponce cnAllCounters = getCnAllCounters(addressHouse.getFias());

            if (cnAllCounters.getAccCntList() == null || cnAllCounters.getAccCntList().size() == 0) {
                logResultInfo("Нет данных о лиц. счетах и  ИПУ");
                continue;
            }

            saveCnAddressLsAndMetersAndValues(cnAllCounters.getAccCntList(), addressHouse);
        }
        logResultInfo("Лицевые счета из ЦН с адресами, данные ИПУ, показания ИПУ приняты.");
        return resultsInfo.toString();

    }

    //@Transactional
    public void cnMeterValuesRepoDeleteAllByToken(String token) throws Exception {
        try {
            cnMeterValuesRepo.deleteAllByToken(token);
        } catch (RuntimeException e) {
            throw new RestServiceToApiCnTransactionException("Не выполнен cnMeterValuesRepo.deleteAllByToken(token) ", e);
        }
    }

    //@Transactional
    public void cnMeterRepoDeleteAllByToken(String token) throws Exception {
        try {
            cnMeterRepo.deleteAllByToken(token);
        } catch (RuntimeException e) {
            throw new RestServiceToApiCnTransactionException("Не выполнен cnMeterRepo.deleteAllByToken(token) ", e);
        }
    }

    //@Transactional
    public void cnAddressLsRepoDeleteAllByToken(String token) throws Exception {
        try {
            cnAddressLsRepo.deleteAllByToken(token);
        } catch (RuntimeException e) {
            throw new RestServiceToApiCnTransactionException("Не выполнен cnAddressLsRepo.deleteAllByToken(token) ", e);
        }
    }

    //@Transactional
    public void saveCnAddressLsAndMetersAndValues(List<AccCnt> accCnts, CnAddressHouseEntity addressHouse) throws Exception {
        for (AccCnt accCnt : accCnts) {
            try {
                //сохраняем данные о лиц. счете
                saveCnAddressLsEntity(addressHouse, accCnt);

                if (accCnt.getCntInfo() == null) continue;  // у лиц счета нет ИПУ
                if (accCnt.getCntInfo().size() == 0) continue;


                for (CntInfo cntInfo : accCnt.getCntInfo()) {
                    // сохраняем данные об ИПУ
                    if (!saveCnMeterEntity(cntInfo, accCnt)) continue;
                    // сохраняем показания ИПУ
                    saveCnMeterValuesEntity(cntInfo, accCnt);
                }

            } catch (RuntimeException e) {
                throw new RestServiceToApiCnTransactionException(addressHouse, e);
            }
        }
    }

    //@Transactional(propagation = Propagation.NESTED)
    private void saveCnMeterValuesEntity(CntInfo cntInfo, AccCnt accCnt) throws Exception, RestServiceToApiCnTransactionException {

        CnMeterValuesEntity cnMeterValuesEntity = new CnMeterValuesEntity();
        for (ScaleInfo scaleInfo : cntInfo.getScaleInfo()) {
            cnMeterValuesEntity.setDevice_id(cntInfo.getID());
            cnMeterValuesEntity.setToken(token);
            cnMeterValuesEntity.setPa(accCnt.getAccount());

            Date indicationDate = scaleInfo.getIndicationDate();
            String indication = scaleInfo.getIndication();
            if (indication == null || indicationDate == null) {
                logResultInfo("Получен null в дате показаний или показаниях. Не приняты показания для лс=" +
                        accCnt.getAccount() +
                        ", device_id= " + cntInfo.getID() +
                        ", scale=" + scaleInfo.getScale());
                continue;
            }
            cnMeterValuesEntity.setIndicationDate(indicationDate); //К сожалению, есть только одна дата показаний....
            switch (scaleInfo.getScale().trim()) {
                case "1": { //Единственные или дневные показания
                    cnMeterValuesEntity.setValue(Double.parseDouble(indication));
                    cnMeterValuesEntity.setValue_night(Double.parseDouble("0"));
                    break;
                }
                case "2": {
                    cnMeterValuesEntity.setValue_night(Double.parseDouble(indication));
                    break;
                }
                default: {
                    logResultInfo("Неверный номер шкалы для ИПУ получен из ЦН для лиц.счета" +
                            accCnt.getAccount() +
                            ", device_id= " + cntInfo.getID() +
                            ", scale=" + scaleInfo.getScale());
                }
            }
        }
        try {
            cnMeterValuesRepo.saveAndFlush(cnMeterValuesEntity); // сохраняем полученные показания ИПУ из ЦН
        } catch (RuntimeException e) {
            throw new RestServiceToApiCnTransactionException(cnMeterValuesEntity, e);
        }

    }

    //@Transactional(propagation = Propagation.NESTED)
    private boolean saveCnMeterEntity(CntInfo cntInfo, AccCnt accCnt) throws Exception, RestServiceToApiCnTransactionException {
        CnMeterEntity cnMeterEntity = new CnMeterEntity();
        cnMeterEntity.setToken(token);
        cnMeterEntity.setDeviceId(cntInfo.getID());
        cnMeterEntity.setInv(cntInfo.getSerialNo());
        cnMeterEntity.setExternal(""); // строка для экспорта. Назначение непонятно
        cnMeterEntity.setName(cntInfo.getCustomName());
        cnMeterEntity.setPa(accCnt.getAccount());
        cnMeterEntity.setPover(cntInfo.getDateOut()); // дата поверки
        cnMeterEntity.setCodeService(cntInfo.getService().getCode()); //код услуги по ЦН
        cnMeterEntity.setNameService(cntInfo.getService().getName()); //наименование услуги по ЦН
        if (cntInfo.getScaleInfo() == null || cntInfo.getScaleInfo().size() == 0) {
            //Для ИПУ из ЦН не пришли данные о показаниях, должны быть сохранены предыущие показания в App
            //не сохраняем их здесь и не передаем в App
            return false;
        }
        cnMeterEntity.setScales(cntInfo.getScaleInfo().size()); //количество шкал
        try {
            cnMeterRepo.saveAndFlush(cnMeterEntity); // Сохраняем данные по ИПУ на адресе
        } catch (RuntimeException e) {
            throw new RestServiceToApiCnTransactionException(cnMeterEntity, e);
        }

        return true;
    }

    //@Transactional(propagation = Propagation.NESTED)
    private void saveCnAddressLsEntity(CnAddressHouseEntity addressHouse, AccCnt accCnt) throws Exception, RestServiceToApiCnTransactionException {
        CnAddressLsEntity cnAddressLsEntity = new CnAddressLsEntity();
        cnAddressLsEntity.setToken(token);
        cnAddressLsEntity.setFias(addressHouse.getFias());
        cnAddressLsEntity.setAccount(accCnt.getAccount());
        cnAddressLsEntity.setAdr(addressHouse.getAddressForApp().trim() + ", " +
                accCnt.getAddress().getFlat().trim());
        try {
            cnAddressLsRepo.saveAndFlush(cnAddressLsEntity); // сохраняем данные по адресу
        } catch (RuntimeException e) {
            throw new RestServiceToApiCnTransactionException(addressHouse, e);
        }

    }

    public List<CnAddressHouseEntity> getAddressHouses() throws Exception {
        List<CnAddressHouseEntity> addressHousesNoSort = cnAddressHouseRepo.findAllByToken(token);
        List<CnAddressHouseEntity> addressHouses = addressHousesNoSort.stream()
                .sorted((a1, a2) -> a1.getAddressForApp().compareTo(a2.getAddressForApp()))
                .collect(Collectors.toList());
        if (addressHouses.size() == 0) {
            throw new RestServiceToApiCnException("Нет домов, принятых от ЦН. Используйте последовательно запросы:" + SEPARATOR +
                    "/apiApp/getCnHouseList" + SEPARATOR +
                    "/apiApp/getAppHouseList" + SEPARATOR +
                    "/apiApp/setSinkIdHouseAppAndCn", null);
        }

        return addressHouses;
    }


    @Override
    //Принимаем данные оборотки по услугам по всем домам из ЦН и пишем их в МП для
    //расчетного месяца. Расчетный месяц получаем как предыдущий месяц от текущей даты
    public String setReceiptsFromCnToApp() throws Exception {
        resultsInfo = new StringBuilder();
        //Адреса
        List<CnAddressHouseEntity> cnAddressHouseEntities = getAddressHouses();

        //дата последнего дня месяца квитанций
        Date month = getCnReceipsDate();

        logResultInfo(" Принимаем из ЦН данные о начислениях и оплатах и передаем в МП для " + month);
        for (CnAddressHouseEntity cnAddressHouseEntity : cnAddressHouseEntities) {
            String addressForApp = cnAddressHouseEntity.getAddressForApp();
            logResultInfo(addressForApp);
            //Запрашиваем список квартир и лицевых счетов по дому ранее полученных.
            //в таблице cn_house, т.е. лицевые счета в МП валидны
            setReceiptsFromCnToAppForId_house(cnAddressHouseEntity.getAppIdHouse(), month, addressForApp);
        }
        logResultInfo("Ok. Начисления и оплаты переданы в МП");
        return resultsInfo.toString();
    }

    protected void setReceiptsFromCnToAppForId_house(String appIdHouse,
                                                     Date month,
                                                     String addressForApp) throws Exception {
        AppExportFlatsResponseDto appExportFlatsResponseDto =
                serviceAppSide.getAppExportFlatsResponseForId_house(appIdHouse);

        List<AppExportFlatLsDto> appExportFlatLsDtos = appExportFlatsResponseDto.getFlats();
        if (appExportFlatLsDtos == null || appExportFlatLsDtos.size() == 0) { //Нет квартир или другая ошибка
            logResultInfo(addressForApp + " Нет квартир или другая ошибка");
            return;
        }

        //Получаем массив лицевых счетов для дома
        List<String> accounts = new ArrayList<>();
        for (AppExportFlatLsDto appExportFlatLsDto : appExportFlatLsDtos) {
            accounts.add(appExportFlatLsDto.getSc());
        }

        //Запрашиваем данные об оборотке из ЦН
        BalanceDetailResponse balanceDetailResponse = getCnBalanceDetailResponse(accounts, month);
        if (balanceDetailResponse == null) {
            logResultInfo(addressForApp + " Получили null в данных от ЦН. Пропускаем");
            return;
        }
        if (balanceDetailResponse.getAnswer().getCode() != 0) {
            logResultInfo(addressForApp + " Ошибка в данных от ЦН." +
                    balanceDetailResponse.getAnswer() + ". Пропускаем");
            return;
        }

        // запрашиваем данные о платежах из ЦН за период
        PaysResponse paysResponse = getCnPaysResponse(accounts, month);
        if (paysResponse.getAnswer().getCode() != 0) {
            logResultInfo(addressForApp + " Ошибка в платежах от ЦН." +
                    paysResponse.getAnswer() + ". Пропускаем");
            return;
        }
        // сформируем последние платежи
        Map<String, Date> cnLastPays = getCnLastPays(paysResponse);

        //Перерабатываем данные из ЦН для использования в МП
        AppImportReceiptsRequestDto appImportReceiptsRequestDto =
                getAppImportReceiptsRequestDto(balanceDetailResponse, cnLastPays);
        if (appImportReceiptsRequestDto.getData().size() == 0) {
            logResultInfo(addressForApp + " Нет данных от ЦН. Пропускаем");
            return;
        }

        sendReceiptsToApp(appImportReceiptsRequestDto);

    }

    private Date getCnReceipsDate() {
        //Дата последнего дня предыдущего месяца от текущей даты
        return myDate.getPreviousMonthLastDate();
    }

    protected Map<String, Date> getCnLastPays(PaysResponse paysResponse) throws Exception {
        //Вернет последний платеж для лицевого счета или
        Map<String, Date> cnLastPays = new HashMap<>();
        if (paysResponse.getPays().getPaysDocuments() == null) {
            return cnLastPays;
        }
        List<PaysDocument> paysDocuments = paysResponse.getPays().getPaysDocuments();
        List<String> accounts = paysDocuments.stream()
                .map(p -> p.getAccount())
                .distinct()
                .collect(Collectors.toList());
        for (PaysDocument paysDocument : paysDocuments) {
            //выделяем максимальную дату платежа из всех платежей лицевого за полученный период
            //Если нет такого - 0 дату
            Date lastPay = (Date) paysDocument.getDocsList().stream()
                    .map(p -> p.getDateVal())
                    .max(Date::compareTo).orElse(getDefaultDate());

            cnLastPays.put(paysDocument.getAccount(), lastPay);
        }
        return cnLastPays;
    }

    private Date getDefaultDate() throws Exception {
        return myDate.StringYyyyMmDdToDate(0, 0, 0);
    }

    protected PaysResponse getCnPaysResponse(List<String> accounts, Date month) throws Exception {
        // запросим интервал оплат на 3 мес назад. Может быть и попадет
        // последний платеж. Иначе вернем null
        int howMonthesBebore = 3;
        Date lastDateOfPayInterval = month; //Дата последнего дня предыдущего месяца от текущей даты
        Date firstDateOfPayInterval = myDate.DateMinusMonthes(month, howMonthesBebore);
        PaysRequest paysRequest = new PaysRequest(accounts, "1", token,
                firstDateOfPayInterval, lastDateOfPayInterval);
        String responseBody = cnApiService.getBodyResponseCn(CN_URL + CN_PAYS,
                objectMapper.writeValueAsString(paysRequest));
        PaysResponse paysResponse = objectMapper.readValue(responseBody, PaysResponse.class);
        return paysResponse;
    }

    private void sendReceiptsToApp(AppImportReceiptsRequestDto appImportReceiptsRequestDto) throws Exception {
        int limit = APP_LIMIT_SIZE_OF_PACKAGE;
        int sizeOfPackge = limit;
        int countPackage = 0;
        //клон объекта сложной структуры
        String stringJson = objectMapper.writeValueAsString(appImportReceiptsRequestDto);
        AppImportReceiptsRequestDto requestDto = objectMapper.readValue(stringJson,
                AppImportReceiptsRequestDto.class);
        //сортируем по лицевым, т.к. потом будет выборка части коллекции
        LinkedList<AppImportReceiptsDataDto> dataDtos = appImportReceiptsRequestDto.getData().stream()
                .sorted((h1, h2) -> h1.getPa().compareTo(h2.getPa()))
                .collect(Collectors.toCollection(LinkedList::new));

        while (sizeOfPackge == limit) {
            List<AppImportReceiptsDataDto> dataToSend = dataDtos.stream()
                    .skip(limit * countPackage).limit(sizeOfPackge)
                    .collect(Collectors.toList());
            sizeOfPackge = dataToSend.size();

            if (sizeOfPackge == 0) break;

            requestDto.setData(dataToSend);
            TimeUnit.SECONDS.sleep(APP_REQUEST_TIMEOUT);
            logResultInfo("Передаем в МП квитанции л.с. с " + dataToSend.get(0).getPa() +
                    " по " + dataToSend.get(dataToSend.size() - 1).getPa());
            appApiService.invokeAppRequest(objectMapper.writeValueAsString(requestDto), APP_RECEIPTS);
            countPackage++; //следующий пакет
        }

    }

    //Перерабатываем данные из ЦН для использования в МП
    //Результ действителен для настройки ONLINE_BY_EPD=да в ЦН
    protected AppImportReceiptsRequestDto getAppImportReceiptsRequestDto(BalanceDetailResponse balanceDetailResponse,
                                                                         Map<String, Date> cnLastPays)
            throws Exception {
        AppImportReceiptsRequestDto appImportReceiptsRequestDto = new AppImportReceiptsRequestDto();
        List<AppImportReceiptsDataDto> appImportReceiptsDataDtos = new ArrayList<>();
        List<AccBalance> accBalances = balanceDetailResponse.getAccBalanceList();
        if (accBalances == null || accBalances.size() == 0) {
            appImportReceiptsRequestDto.setData(Arrays.asList());
            return appImportReceiptsRequestDto;
        }
        for (AccBalance accBalance : accBalances) {
            List<Balance> balances = accBalance.getBalanceList();
            List<AppImportReceiptServiceDto> serviceDtos = new ArrayList<>();

            SumForAppReceiptsRequest sums = new SumForAppReceiptsRequest(0, 0, 0, 0, 0, 0, 0);
            if (balances == null || balances.size() == 0) {
                serviceDtos.add(new AppImportReceiptServiceDto("Нет данных.", 0, 0));
            } else {
                addBalancesToServiceDtos(serviceDtos, balances, sums);
            }
            AppImportReceiptsDataDto dataDto = new AppImportReceiptsDataDto();
            dataDto.setService(serviceDtos);
            //Формируем уникальный uuid для App
            dataDto.setUuid(getUniqueUUidForAccountAndDate(accBalance));
            dataDto.setPa(accBalance.getAccount());
            dataDto.setType(1);

            dataDto.setDebt(sums.getDebtSum());
            dataDto.setPayments(sums.getPaymentsSum());
            dataDto.setAccrued(sums.getAccruedSum());
            dataDto.setTotal(sums.getTotalSum());
            dataDto.setDate(myDate.getFirstDayMonth(accBalance.getMonth()));
            dataDto.setPaymentsDate(getPaymentsDate(accBalance.getAccount(), cnLastPays));

            appImportReceiptsDataDtos.add(dataDto);

        }
        appImportReceiptsRequestDto.setData(appImportReceiptsDataDtos);
        appImportReceiptsRequestDto.setTs(myDate.nowDate());

        return appImportReceiptsRequestDto;
    }

    private Date getPaymentsDate(String account, Map<String, Date> cnLastPays) throws Exception {
        if (cnLastPays.containsKey(account)) return cnLastPays.get(account);
        else return getDefaultDate();
    }

    private void addBalancesToServiceDtos(List<AppImportReceiptServiceDto> serviceDtos,
                                          List<Balance> balances, SumForAppReceiptsRequest sums) {
        // в ЦН итоги разделены по услугам. Для МП нужно суммировать
        for (Balance balance : balances) {
            String name = balance.getServiceInfo().getService().getName();
            sums.setTotalSum(sums.getTotalSum() +
                    Math.round((balance.getSaldoin() + balance.getCharge() +
                            balance.getPayment() + balance.getMaket() +
                            balance.getSetSum() + balance.getFine()) * 100.0));
            //платежи приходят как отрицательные
            sums.setSetSum(sums.getSetSum() + Math.round(balance.getSetSum() * 100.0));
            sums.setFineSum(sums.getFineSum() + Math.round(balance.getFine() * 100.0));
            sums.setDebtSum(sums.getDebtSum() + Math.round(balance.getSaldoin() * 100.0));
            sums.setPaymentsSum(sums.getPaymentsSum() + Math.round(balance.getPayment() * 100.0));

            long accrued = Math.round(balance.getCharge() * 100.0);
            sums.setAccruedSum(sums.getAccruedSum() + accrued);

            long recalculation = Math.round(balance.getMaket() * 100.0);
            //В данных из ЦН могут быть нулевые начисления (Например:услуга более не оказывается) и
            // полные повторы (Например: услуга уже не оказывается другим поставщиком)
            // Очистим данные для МП
            AppImportReceiptServiceDto serviceDto = new AppImportReceiptServiceDto(name, accrued, recalculation);
            if (notZeroAccuredAndRecalculation(serviceDto)) {
                serviceDtos.add(serviceDto);
            }
        }
        if (!(sums.getFineSum() == 0)) {
            serviceDtos.add(new AppImportReceiptServiceDto("ПЕНИ", sums.getFineSum(), 0));
        }
        if (!(sums.getSetSum() == 0)) {
            serviceDtos.add(new AppImportReceiptServiceDto("КОРР.САЛЬДО", sums.getSetSum(), 0));
        }
    }

    private boolean notZeroAccuredAndRecalculation(AppImportReceiptServiceDto serviceDto) {
        return !(serviceDto.getAccured() == 0 && serviceDto.getRecalculation() == 0);
    }

    //лицевой и строка из даты и дата и время отправки. Используется как уникальный индентификатор для лицевого и месяца
    //поскольку в МП каждая отправка должна быть уникальной и исправления отправок не допускаются
    //при ошибках отправлем НОВУЮ квитанцию
    private String getUniqueUUidForAccountAndDate(AccBalance accBalance) {
        return (accBalance.getAccount() + "-" +
                DateFormatUtils.format(myDate.getFirstDayMonth(accBalance.getMonth()),
                        "yyyyMMdd") + "-" +
                DateFormatUtils.format(myDate.nowDate(), "MMddhhmm"));
    }

    protected BalanceDetailResponse getCnBalanceDetailResponse(List<String> accounts, Date month) throws Exception {
        String url = CN_URL + CN_BALANCEDETAIL;
        BalanceDetailRequest balanceDetailRequest = new BalanceDetailRequest();
        balanceDetailRequest.setAccount(accounts);
        balanceDetailRequest.setAccountTypeReturn("1"); //шифр ЛС из СН
        balanceDetailRequest.setToken(token);
        balanceDetailRequest.setMonth(month);//Дата последнего дня предыдущего месяца от текущей даты

        String rBodyCn = cnApiService.getBodyResponseCn(url, objectMapper.writeValueAsString(balanceDetailRequest));

        BalanceDetailResponse balanceDetailResponse = objectMapper.readValue(rBodyCn, BalanceDetailResponse.class);
        return balanceDetailResponse;
    }

    private AllCountersResponce getCnAllCounters(String fias) throws Exception {
        String url = CN_URL + CN_ALLCOUNTERS;

        AllCountersRequest allCountersRequest = new AllCountersRequest();
        allCountersRequest.setAccount(null); // Идентификатор ЛС. необязательный
        //noinspection ArraysAsListWithZeroOrOneArgument
        allCountersRequest.setFias(Arrays.asList(fias)); //Один дом
        allCountersRequest.setAllAcc(1); //вернуть все лиц.счета; в том числе без ИПУ
        allCountersRequest.setSource(token);

        String body;
        try {
            body = objectMapper.writeValueAsString(allCountersRequest);
        } catch (Exception e) {
            throw new RuntimeException(e.toString());
        }

        //Возможен Exception при выполнении запросов
        String rBody = cnApiService.getBodyResponseCn(url, body);

        AllCountersResponce allCountersResponce;
        try {

            allCountersResponce = objectMapper.readValue(rBody, AllCountersResponce.class);
        } catch (Exception e) {
            throw new RestServiceToApiCnException("Ошибка Json при разборе дома " + fias + ". " + e.toString(), e);
        }

        if (allCountersResponce == null)
            throw new RestServiceToApiCnException("Ошибка в данных, полученных с сервера. Запрос по дому " + fias, null);

        if (allCountersResponce.getAnswer().getCode() != 0)
            throw new RestServiceToApiCnException("Ошибка в данных, полученных с сервера. Сообщение - " +
                    allCountersResponce.getAnswer().getMessage(), null);

        return allCountersResponce;
    }

    /*
     * Делает запрос на ЦН о данных ИПУ по дому
     * @param fias - ФИАС дома
     * @return Объект Список данных об ИПУ
     * @throws Exception RestServiceToApiCnException something
     */

    /**
     * Делает запрос на ЦН за списком домов
     *
     * @return Объект Список домов ЦН
     */
    private CnHouseListResponceDto getCnHouseListResponce() throws Exception {
        CnHouseListRequestDto cnHouseListRequestDto = new CnHouseListRequestDto();
        cnHouseListRequestDto.setToken(token);
        String body;
        try {
            body = objectMapper.writeValueAsString(cnHouseListRequestDto);
        } catch (Exception e) {
            throw new RestServiceToApiCnException(e.getMessage(), e);
        }
        String rBody = cnApiService.getBodyResponseCn(CN_URL + CN_HOUSELIST, body);

        CnHouseListResponceDto cnHouseListResponceDto;
        try {
            cnHouseListResponceDto = objectMapper.readValue(rBody, CnHouseListResponceDto.class);
        } catch (Exception e) {
            throw new RestServiceToApiCnException("Ошибка Json при разборе списка домов. " + e.toString(), e);
        }
        if (cnHouseListResponceDto == null)
            throw new RestServiceToApiCnException("Ошибка в данных, полученных с сервера" +
                    ". Запрос на список домов.", null);
        if (cnHouseListResponceDto.getAnswer().getCode() != 0)
            throw new RestServiceToApiCnException("Ошибка в данных, полученных с сервера" +
                    ". Запрос на список домов. Сообщение - " +
                    cnHouseListResponceDto.getAnswer().getMessage(), null);


        return cnHouseListResponceDto;
    }

    private AllCountersResponce getCnHouseAllCounters(String fias) throws Exception {

        AllCountersRequest allCountersRequest = new AllCountersRequest();
        allCountersRequest.setAccount(null); // Идентификатор ЛС. необязательный
        //noinspection ArraysAsListWithZeroOrOneArgument
        allCountersRequest.setFias(Arrays.asList(fias)); //Один дом
        allCountersRequest.setAllAcc(1); //вернуть все лиц.счета; в том числе без ИПУ
        allCountersRequest.setSource(token);
        String body;
        try {
            body = objectMapper.writeValueAsString(allCountersRequest);
        } catch (Exception e) {
            throw new RestServiceToApiCnException(e.toString(), e);
        }

        String rBody = cnApiService.getBodyResponseCn(CN_URL + CN_ALLCOUNTERS, body);

        AllCountersResponce allCountersResponce;
        try {

            allCountersResponce = objectMapper.readValue(rBody, AllCountersResponce.class);
        } catch (Exception e) {
            throw new RestServiceToApiCnException("Ошибка Json при разборе дома " + fias + ". " + e.toString(), e);
        }
        if (allCountersResponce == null)
            throw new RestServiceToApiCnException("Ошибка в данных, полученных с сервера. Запрос по дому " + fias, null);

        return allCountersResponce;
    }

    @Override
    /**
     *  Собственно, выполнение синхронизации данных между ЦН и МП
     *  В процессе возможны Exception. Синхронизация может быть выполнена частично.
     *  В этом случае вернется Body
     *  Логирование ведет каждый метод
     *  whatDo - "all" - запишем в ЦН показания по всем услугам
     *          -"for" - запишем в ЦН показания по услугам из списка serviceList
     *          -"without" - запишем в ЦН показания по услугам не вошедшим в список serviceList
     */
    public String doSinkCnAndApp() throws Exception {
        String result;
        StringBuilder message = new StringBuilder();


        try {
            message.append(myDate.dtocShort(myDate.today()))
                    .append(SEPARATOR)
                    .append(" Начали синхронизацию.");

            result = test();
            message.append(result).append(SEPARATOR);
            log.info(result);
            if (!result.contains("Ok. ")) {
                sendMail("Error synchronization", message.toString());
                return "Error synchronization";
            }


            //Получаем из ЦН список домов
            message.append(getCnHouseList()).append(SEPARATOR);

            //Получаем из МП список домов
            message.append(serviceAppSide.getAppHouseList()).append(SEPARATOR);

            //установим соответстие домов МП и ЦН
            result = setSinkIdHouseAppAndCn();
            message.append(result).append(SEPARATOR);
            log.info(result);
            if (!result.contains("Ok. ")) { //это значит, что есть дома, которые отстуствуют в МП
                sendMail("Error synchronization", message.toString());
                return "Error synchronization";
            }

            //Принимаем лицевые из МП
            message.append(serviceAppSide.getAppLsList()).append(SEPARATOR);

            //Принимаем показания из МП
            message.append(serviceAppSide.getAppValues()).append(SEPARATOR);

            //Принимаем из ЦН лицевые, приборы, показания
            message.append(getCnLsAndCounters()).append(SEPARATOR);

            //Записываем в МП новые лицевые
            message.append(serviceAppSide.setAppNewLs()).append(SEPARATOR);

            //Записываем в МП ИПУ и показания
            message.append(serviceAppSide.setAppMeters()).append(SEPARATOR);
            message.append(serviceAppSide.setAppValues()).append(SEPARATOR);

            //Записываем в ЦН показания в зависимости от параметров whatDo и serviceList
            message.append(setCnValues()).append(SEPARATOR);
            sendMail("Ok synchronization", message.toString());

            return "Ok synchronization";
        } catch (Exception e) {
            sendMail("There is Exception.", message.toString() + SEPARATOR +
                    "Error synchronization. Exception " + e.getMessage() + " " + e.getCause());
            throw new RestServiceToApiCnException("Неожиданная ошибка при синхронизации", e);
        }
    }

    @SuppressWarnings("RedundantThrows")
    protected void sendMail(String result, String message) throws Exception {
        //учитываем, что subject принят из propеrty file, а тот в такой кодировке.
        String emailSubject = new String(EMAIL_SUBJECT.getBytes(StandardCharsets.ISO_8859_1));
        emailService.send(SEND_TO_EMAIL, emailSubject, result + SEPARATOR + message, false);
    }

    @Override
    public void setWhatDo(String whatDo) {
        this.whatDo = whatDo;
    }

    @Override
    public void setServiceList(ServiceListDto serviceList) throws Exception {
        if (serviceList == null ||
                serviceList.getServiceList() == null ||
                serviceList.getServiceList().size() == 0) {
            throw new RestServiceToApiCnException("Список кодов услуг по ЦН в запросе должен быть. Например, {\"serviceList\":[\"010\", \"2\"]}", null);
        }

        this.serviceList = serviceList;
    }

    @Data
    @AllArgsConstructor
    protected class SumForAppReceiptsRequest {
        private long debtSum;      //"задолженность в копейках"  //sum saldoin
        private long paymentsSum; //"внесено оплат, в копейках"
        private long accruedSum;  // "начислено оплат, в копейках"
        private long totalSum;    //"итого к оплате, в копейках"
        private long fineSum;     //начислено пени в копейках
        private long setSum;      //коректировки сальдо
        private long setFine;     //начисление сальдо

    }
}
