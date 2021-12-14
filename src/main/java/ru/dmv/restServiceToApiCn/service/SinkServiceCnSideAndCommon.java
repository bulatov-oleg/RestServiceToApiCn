package ru.dmv.restServiceToApiCn.service;

import org.springframework.stereotype.Component;
import ru.dmv.restServiceToApiCn.dto.ServiceListDto;

@Component
public interface SinkServiceCnSideAndCommon {
    SinkServiceCnSideAndCommon getInstance();

    String test() throws Exception;

    String setReceiptsFromCnToApp() throws Exception;

    String getCnHouseList() throws Exception;

    String getCnLsAndCounters() throws Exception;

    String setSinkIdHouseAppAndCn() throws Exception;

    String setCnValues() throws Exception;

    String doSinkCnAndApp() throws Exception;

    void setWhatDo(String whatDo);

    void setServiceList(ServiceListDto serviceList) throws Exception;

}
