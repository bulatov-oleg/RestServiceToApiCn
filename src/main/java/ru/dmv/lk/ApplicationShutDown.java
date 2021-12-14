package ru.dmv.lk;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.dmv.lk.controller.RegistrationController;

import java.util.concurrent.ExecutorService;
@Component
public class ApplicationShutDown implements DisposableBean {
    @Autowired
    ExecutorService executorService;

    private static Logger log = LoggerFactory.getLogger(RegistrationController.class);

    @Override
    public void destroy() throws Exception {
        log.info("Start of shutdown this application");

        executorService.shutdownNow();
        log.info("End of shutdown  this application");

    }
}
