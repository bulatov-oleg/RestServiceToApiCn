package ru.dmv.restServiceToApiCn.repository;

import org.springframework.stereotype.Repository;
import ru.dmv.restServiceToApiCn.model.CnMeterEntity;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;

@Repository
public class CnMeterSqlRepoImpl implements CnMeterSqlRepo {

    @PersistenceContext
    EntityManager entityManager;

    @SuppressWarnings("unchecked")
    @Override
    public ArrayList<CnMeterEntity> findByTokenOrderedByPaLimitedToOffsetTo(String token, int limit, int offset) {
        return (ArrayList<CnMeterEntity>) entityManager.createNativeQuery(
                "select * " +
                        "from cn_meters " +
                        " where token= ?1" +
                        " order by pa",
                CnMeterEntity.class)
                .setParameter(1, token)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }
}
