package ru.dmv.lk;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableScheduling  //Разрешение работы по расписанию для SchedulerService
@SpringBootApplication
@EnableTransactionManagement
/*
@ComponentScan({"ru.dmv.lk","ru.dmv.cnNovosibirsk", "ru.dmv.restServiceToApiCn"})
@EntityScan({"ru.dmv.lk","ru.dmv.cnNovosibirsk","ru.dmv.restServiceToApiCn"})
*/
@ComponentScan({"ru.dmv"})
@EntityScan({"ru.dmv"})
@EnableJpaRepositories(basePackages="ru.dmv")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

