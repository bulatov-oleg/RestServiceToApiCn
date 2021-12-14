package ru.dmv.restServiceToApiCn.repository;

import org.springframework.stereotype.Repository;
import ru.dmv.restServiceToApiCn.model.CnMeterValuesEntity;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;


@Repository
public class CnMeterValuesSqlRepoImpl implements CnMeterValuesSqlRepo {

    @PersistenceContext
    EntityManager entityManager;

    @SuppressWarnings("unchecked")
    @Override
    public ArrayList<CnMeterValuesEntity> findByTokenOrderedByPaLimitedToOffsetTo(
            String token, int limit, int offset) {
        return (ArrayList<CnMeterValuesEntity>) entityManager.createNativeQuery(
                "select * " +
                        "from cn_meters_values" +
                        " where token= ?1" +
                        " order by pa",
                CnMeterValuesEntity.class)
                .setParameter(1, token)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }
}
