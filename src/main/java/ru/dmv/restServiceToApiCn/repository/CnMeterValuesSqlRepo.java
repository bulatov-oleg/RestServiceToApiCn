package ru.dmv.restServiceToApiCn.repository;

import org.springframework.stereotype.Repository;
import ru.dmv.restServiceToApiCn.model.CnMeterValuesEntity;

import java.util.ArrayList;

@Repository
public interface CnMeterValuesSqlRepo {
    ArrayList<CnMeterValuesEntity> findByTokenOrderedByPaLimitedToOffsetTo(String token, int limit, int offset);
}
