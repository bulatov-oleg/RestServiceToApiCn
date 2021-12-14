package ru.dmv.restServiceToApiCn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.dmv.restServiceToApiCn.model.AppMeterValuesEntity;

import java.util.List;

@Repository
@Transactional
public interface AppMetersValuesRepo extends JpaRepository<AppMeterValuesEntity,Long> {
    void deleteAllByToken(String token);
    List<AppMeterValuesEntity> findAllByToken(String token);
    AppMeterValuesEntity findById(long id);
}
