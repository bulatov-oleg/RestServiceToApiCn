package ru.dmv.restServiceToApiCn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.dmv.restServiceToApiCn.model.CnMeterValuesEntity;

@Repository
@Transactional
public interface CnMeterValuesRepo extends JpaRepository<CnMeterValuesEntity, Long>,
        CnMeterValuesSqlRepo {
    void deleteAllByToken(String token);

}
