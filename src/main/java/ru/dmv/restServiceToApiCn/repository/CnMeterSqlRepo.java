package ru.dmv.restServiceToApiCn.repository;

import org.springframework.stereotype.Repository;
import ru.dmv.restServiceToApiCn.model.CnMeterEntity;

import java.util.ArrayList;

@Repository
public interface CnMeterSqlRepo {
    ArrayList<CnMeterEntity> findByTokenOrderedByPaLimitedToOffsetTo(String token, int limit, int offset);
}
