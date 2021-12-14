package ru.dmv.lk;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ru.dmv.lk.controller.RegistrationController;
import ru.dmv.lk.dto.MyAppConst;
import ru.dmv.lk.service.UserServiceImp;

import java.nio.charset.StandardCharsets;

@Component
public class ApplicationStartup implements CommandLineRunner {

    @Value("${app.project.name}")
    private String pN;
    @Value("${app.project.date}")
    private String pD;


    private final UserServiceImp userService;
    //private final MyAppConst myAppConst;
    @Autowired
    public ApplicationStartup(UserServiceImp userService){
        this.userService=userService;
      //  this.myAppConst=myAppConst;
    }

    private static Logger log = LoggerFactory.getLogger(RegistrationController.class);

    @Override
    public void run(String... arg0)  {
            // write your logic here
        log.info("Start action after StartUp this application");
        // Приложению требуется User admin, apilkuser и primer, со всеми таблицами для отладки и примера
        userService.addUsefullUsers();
        MyAppConst myAppConst=new MyAppConst();
        myAppConst.setNameApp(new String(pN.getBytes(StandardCharsets.ISO_8859_1)));
        myAppConst.setYearDeveloping(new String(pD.getBytes(StandardCharsets.ISO_8859_1)));
        log.info("End action after StartUp this application");
    }
}

