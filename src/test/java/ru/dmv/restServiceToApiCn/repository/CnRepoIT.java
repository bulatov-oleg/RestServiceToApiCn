package ru.dmv.restServiceToApiCn.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import ru.dmv.restServiceToApiCn.model.CnMeterEntity;
import ru.dmv.restServiceToApiCn.model.CnMeterValuesEntity;
import ru.dmv.lk.utils.MyUtil;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = ru.dmv.lk.Application.class)
@AutoConfigureMockMvc
@TestPropertySource(locations = "file:./src/main/resources/application-dev.properties")
class CnRepoIT {

    private ArrayList<CnMeterEntity> list;
    private ArrayList<CnMeterValuesEntity> list1;

    private CnMeterRepo cnMeterRepo;
    private CnMeterValuesRepo cnMeterValuesRepo;
    private MyUtil myUtil;

    @SuppressWarnings("SpringJavaAutowiredMembersInspection")
    @Autowired
    CnRepoIT(CnMeterRepo cnMeterRepo,
             CnMeterValuesRepo cnMeterValuesRepo,
             MyUtil myUtil) {
        this.cnMeterRepo = cnMeterRepo;
        this.cnMeterValuesRepo = cnMeterValuesRepo;
        this.myUtil = myUtil;
    }

    @Test
    @Transactional
    void cnMeterRepoFindByTokenOrderedByPaLimitedToOffsetTo() {
        String token = "test";
        cnMeterRepo.deleteAllByToken(token);
        for (int i = 601; i > 0; i--) {
            CnMeterEntity cnMeterEntity = new CnMeterEntity();
            cnMeterEntity.setToken(token);
            cnMeterEntity.setPa(myUtil.padLeftByChars(Integer.toString(i), 10, '0'));
            cnMeterRepo.saveAndFlush(cnMeterEntity);
        }

        list = cnMeterRepo.findByTokenOrderedByPaLimitedToOffsetTo(token, 300, 0);
        assertAll("Контроль размера и первого элемента пакета",
                () -> assertEquals(list.size(), 300, "Wrong size of package"),
                () -> assertEquals(list.get(0).getPa().trim(), "0000000001", "Wrong first element of package"));

        list = cnMeterRepo.findByTokenOrderedByPaLimitedToOffsetTo(token, 300, 600);
        assertAll("Контроль размера и последего элемента пакета",
                () -> assertEquals(list.size(), 1, "Size of last package not equal 1"),
                () -> assertEquals(list.get(0).getPa().trim(), "0000000601", "Wrong last element of data"));

    }

    @Test
    @Transactional
    void cnMeterValuesRepoFindByTokenOrderedByPaLimitedToOffsetTo() {
        String token = "test";
        cnMeterValuesRepo.deleteAllByToken(token);
        for (int i = 601; i > 0; i--) {
            CnMeterValuesEntity cnMeterValuesEntity = new CnMeterValuesEntity();
            cnMeterValuesEntity.setToken(token);
            cnMeterValuesEntity.setPa(myUtil.padLeftByChars(Integer.toString(i), 10, '0'));
            cnMeterValuesRepo.saveAndFlush(cnMeterValuesEntity);
        }

        list1 = cnMeterValuesRepo.findByTokenOrderedByPaLimitedToOffsetTo(token, 300, 0);
        assertAll("Контроль размера и первого элемента пакета",
                () -> assertEquals(list1.size(), 300, "Wrong size of package"),
                () -> assertEquals(list1.get(0).getPa().trim(), "0000000001", "Wrong first element of package"));
        list1 = cnMeterValuesRepo.findByTokenOrderedByPaLimitedToOffsetTo(token, 300, 600);
        assertAll("Контроль размера и последего элемента пакета",
                () -> assertEquals(list1.size(), 1, "Size of last package not equal 1"),
                () -> assertEquals(list1.get(0).getPa().trim(), "0000000601", "Wrong last element of data"));

    }
}