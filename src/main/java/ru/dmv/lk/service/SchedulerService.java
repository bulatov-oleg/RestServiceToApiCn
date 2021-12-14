package ru.dmv.lk.service;

import org.springframework.scheduling.annotation.Scheduled;

public interface SchedulerService {
    //https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/scheduling/support/CronSequenceGenerator.html
    @Scheduled(cron = "0 * 4 * * *") // в 4.00 ежедневно
    void controlTimeActivation();
}
