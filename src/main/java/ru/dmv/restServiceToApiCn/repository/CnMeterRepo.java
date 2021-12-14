package ru.dmv.restServiceToApiCn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.dmv.restServiceToApiCn.model.CnMeterEntity;

import java.util.LinkedList;
import java.util.List;

@Repository
@Transactional
public interface CnMeterRepo extends JpaRepository<CnMeterEntity, Long>, CnMeterSqlRepo {
    void deleteAllByToken(String token);
    CnMeterEntity findByTokenAndDeviceId(String token,String device_id);
    List<CnMeterEntity> findAllByToken(String token);
    LinkedList<CnMeterEntity> findAll();
}
