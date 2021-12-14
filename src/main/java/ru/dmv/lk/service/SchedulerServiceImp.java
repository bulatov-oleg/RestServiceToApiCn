package ru.dmv.lk.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.dmv.lk.controller.RegistrationController;
import ru.dmv.lk.model.User;

import java.util.ArrayList;


@Service
public class SchedulerServiceImp implements SchedulerService {

    @Autowired
    private UserService userService;

    private static Logger log = LoggerFactory.getLogger(RegistrationController.class);

    //https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/scheduling/support/CronSequenceGenerator.html
    @Override
    @Scheduled(cron = "0 * 4 * * *") // в 4.00 ежедневно
    public void controlTimeActivation(){
        ArrayList<User> deletedUsers = userService.deleteUsersWithOverdueActivationCode(1);
        for (User user : deletedUsers) {
            log.info("Сервисом ShedulerService.controlTimeActivation() Удален пользователь "+user.getUsername()+". Причина: не подтвердил активацию по e-mail");
        }
    }
    //
}
