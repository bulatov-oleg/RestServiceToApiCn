package ru.dmv.restServiceToApiCn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.dmv.restServiceToApiCn.model.AppAddressLsEntity;

import java.util.List;

@Repository
@Transactional
public interface AppAddressLsRepo extends JpaRepository<AppAddressLsEntity,Long> {
    void deleteAllByToken(String token);
    List<AppAddressLsEntity> findAllByToken(String token);
}
