package ru.dmv.lk.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.dmv.lk.dto.MyAppConst;
import ru.dmv.lk.model.User;
import ru.dmv.lk.service.UserService;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;


@Controller
@RequestMapping("/user")

public class UserController {
    @Autowired
    private UserService userService;

    private static Logger log = LoggerFactory.getLogger(RegistrationController.class);



    @GetMapping("/user_context_form")
    // Изменение данных User. маппинг для ссылки из header_bs.html
    public String userContextForm(User user, Map<String, Object> model) {
        model.put("MyApp", new MyAppConst());
        return "user/user_context_form";
    }

    @PostMapping("/user_context_change")
    // Изменение данных User. Действия по submit /user/user_context_form.html
    public String userContex(Map<String, Object> model,
                             HttpServletRequest request,
                             @RequestParam Map<String, String> form) {
        model.put("MyApp", new MyAppConst());
        Map<String, String[]> parametersFormMap = request.getParameterMap(); //Получили доступ к переменным формы
        StringBuilder errorsText = new StringBuilder();
        User user = userService.getUserByUsername(form.get("username"));
        if (user == null) {
            errorsText.append(" Неверно указаны имя пользователя и/или пароль.<br />");
        } else {
            if (parametersFormMap.get("password_new")[0].length() < 6) {
                errorsText.append("Пароль должен быть 6 или более символов.<br />");
            }
            if (parametersFormMap.containsKey("repeatedPassword_new")) { //на форме есть поле с наименованием?
                if (!parametersFormMap.get("password_new")[0].trim().equals(parametersFormMap.get("repeatedPassword_new")[0].trim())) {
                    errorsText.append("Пароль не совпал с повтором пароля.<br />");
                }
            }
        }
        if (errorsText.length() != 0) {
            model.put("message", errorsText);  //ошибки все же были...
            model.put("MyApp", new MyAppConst());
            return "user/user_context_form";
        }

        // Изменим пароль в базе
        userService.setPassword(user, parametersFormMap.get("password_new")[0]);
        userService.saveUser(user);
        log.info("Пользователь " + user.getUsername() + " изменил пароль ");
        return "/index";
    }

    @GetMapping("/make_email")
    public String makeEmail(Map<String, Object> model,
                            HttpServletRequest request,
                            @RequestParam Map<String, String> form){
        String username = request.getRemoteUser();
        User user = userService.getUserByUsername(username);
        model.put("MyApp", new MyAppConst());
        model.put("Function", "make");
        if (user != null) {
            try {
                return userService.makeEmail(user, model, form); //="/user/write_email" ожидается
            }catch (Exception e) {
                e.printStackTrace();
                return "/index";
            }
        }else
            return "/index";
    }

    @PostMapping("/send_email")
    public String sendEmail(Map<String, Object> model,
                        HttpServletRequest request,
                        @RequestParam Map<String, String> form){
        String username = request.getRemoteUser();
        User user = userService.getUserByUsername(username);
        model.put("MyApp", new MyAppConst());
        model.put("Function", "sent");
        if (user != null) {
            try {
                return userService.makeEmail(user, model, form); //="/user/write_email" ожидается
            }catch (Exception e) {
                e.printStackTrace();
                return "/index";
            }
        }else
            return "/index";
    }
}
