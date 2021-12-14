package ru.dmv.restServiceToApiCn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.dmv.restServiceToApiCn.model.CnAddressLsEntity;

import java.util.List;

@Repository
@Transactional
public interface CnAddressLsRepo extends JpaRepository<CnAddressLsEntity,Long> {

    List<CnAddressLsEntity> findAllByToken(String token);
    void deleteAllByToken(String token);
    CnAddressLsEntity findByTokenAndAccount(String token, String account);
}
