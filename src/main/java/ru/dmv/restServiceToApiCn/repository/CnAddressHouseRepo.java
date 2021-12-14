package ru.dmv.restServiceToApiCn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.dmv.restServiceToApiCn.model.CnAddressHouseEntity;

import java.util.List;


@Repository
@Transactional
public interface CnAddressHouseRepo extends JpaRepository<CnAddressHouseEntity, Long> {
    void deleteAddressHouseCnEntitiesByToken(String token);
    List<CnAddressHouseEntity> findAllByToken(String token);

}
