package ru.dmv.restServiceToApiCn.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.dmv.restServiceToApiCn.model.AppHouseEntity;

import java.util.List;

@Repository
@Transactional
public interface AppHouseRepo extends JpaRepository<AppHouseEntity,Long> {
    void deleteAllByToken(String token);

    List<AppHouseEntity> findAllByTokenOrderByAddress(String token);
    AppHouseEntity findByTokenAndAddress(String token, String address);
}
