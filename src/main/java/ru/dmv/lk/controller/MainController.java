package ru.dmv.lk.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import ru.dmv.lk.dto.MyAppConst;
import ru.dmv.lk.model.User;
import ru.dmv.lk.service.UserServiceImp;
import ru.dmv.lk.utils.MyDate;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.util.Map;

@Controller
public class MainController {
   @Autowired
    UserServiceImp userService;
   @Autowired
   MyDate myDate;

    @GetMapping(value = {"/", "/index"})
    public String greeting(Map<String, Object> model, HttpServletRequest request) {
        String username = request.getRemoteUser();
        if (username != null){
            User user = userService.getUserByUsername(username); //может быть так, что username пришел, но в базе его нет!
            if (user!=null){
                LocalDate wasActiveDate = user.getWasActive();
                if (wasActiveDate==null){wasActiveDate=LocalDate.now();}
                Locale locale = Locale.getDefault();
                String wasActiveDateFmt = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).withLocale(locale).toString();
                model.put("wasActiveDate", myDate.dtocShort(wasActiveDate));
            }else return "error_403";
        }
        model.put("MyApp", new MyAppConst());
        return "index";
    }



    @GetMapping("/errors/error") //Ошибка
    public String error(Map<String, Object> model) {
        model.put("MyApp", new MyAppConst());
        return "errors/error";
    }

    @GetMapping("/403")
    public String err_403(Map<String, Object> model) {
        model.put("MyApp", new MyAppConst());
        return "errors/error_403";
    }
}
