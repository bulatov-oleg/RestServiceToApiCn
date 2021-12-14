package ru.dmv.lk.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.authentication.logout.CookieClearingLogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.authentication.rememberme.AbstractRememberMeServices;
import org.springframework.web.bind.annotation.*;
import ru.dmv.lk.dto.MyAppConst;
import ru.dmv.lk.model.User;
import ru.dmv.lk.service.EmailService;
import ru.dmv.lk.utils.PasswordGenerator;
import ru.dmv.lk.utils.SimpleEmailValidator;
import ru.dmv.lk.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Set;

@Controller
@RequestMapping("/registration")
public class RegistrationController {
    @Autowired
    private UserService userService;
    @Autowired
    private SimpleEmailValidator emailValidator;
    @Autowired
    private EmailService emailService;
    @Autowired
    private PasswordGenerator passwordGenerator;

    private static Logger log = LoggerFactory.getLogger(RegistrationController.class);
    //Spring Security see this :



    @GetMapping("/msglogin/{msg}")
    public String msglogin (Map<String, Object> model,@PathVariable String msg){
        model.put("MyApp", new MyAppConst()); //Для header_bs.html и footer.html
        model.put(msg, ""); //Для header_bs.html и footer.html
        return "index";
    }

     @GetMapping("/logout")
     public String myLogoff(HttpServletRequest request, HttpServletResponse response,
                            Map<String, Object> model) {
         CookieClearingLogoutHandler cookieClearingLogoutHandler = new CookieClearingLogoutHandler(AbstractRememberMeServices.SPRING_SECURITY_REMEMBER_ME_COOKIE_KEY);
         SecurityContextLogoutHandler securityContextLogoutHandler = new SecurityContextLogoutHandler();
         cookieClearingLogoutHandler.logout(request, response, null);
         securityContextLogoutHandler.logout(request, response, null);
         model.put("MyApp", new MyAppConst()); //Для header_bs.html и footer.html
         return "registration/logout";
     }


    @PostMapping("/adduser")
    // действия по submit из /registration/registration_form.html
    public String addUser(User user, Map<String, Object> model,
                          HttpServletRequest request,
                          @RequestParam Map<String,String> form) {
        //при входе с формы уже существует user c введенным именем и параметрами. Почему - непонятно!
        //Если ничего не введем на форму, то username="", password=",", email=""
       //Map<String,String[]> parametersFormMap = request.getParameterMap(); //Получили доступ к переменным формы
        Map<String,String[]> parametersFormMap = request.getParameterMap(); //Получили доступ к переменным формы
        model.put("MyApp", new MyAppConst()); //Для header_bs.html и footer.html
        model.put("username",form.getOrDefault("username",""));
        model.put("password",form.getOrDefault("password",""));
//        model.put("repeatedPassword",form.containsKey("repeatedPassword")?form.get("repeatedPassword"):"");
        model.put("email",form.getOrDefault("email",""));
        model.put("repeatedEmail",form.getOrDefault("repeatedEmail",""));

        StringBuilder errorsText = new StringBuilder();
        if (user.getUsername().equals("")){
            errorsText.append("Имя пользователя должно быть указано.<br />");
        }
        if (user.getPassword().length()<6){
            errorsText.append("Пароль должен быть 6 или более символов.<br />");
        }
        if (user.getEmail().equals("")) {
            errorsText.append("E-mail должен быть указан.<br />");
        }else{
            if (!emailValidator.simpleValidate(user.getEmail())) {
                errorsText.append("E-mail не соотвествует шаблону. В нем нет @ и точки.<br />");
            }
        }
        if (parametersFormMap.containsKey("repeatedPassword")){ //на форме есть поле с наименованием?
            if (!user.getPassword().trim().equals(parametersFormMap.get("repeatedPassword")[0].trim())) {
                errorsText.append("Пароль не совпал с повтором пароля.<br />");
            }
        }

        if (userService.isUserName(user)) {
            errorsText.append("Имя пользователя ").append(user.getUsername().trim()).append(" уже использовано.<br />");
        }
        if (userService.isUserEmail(user)) {
            errorsText.append("E-mail ").append(user.getEmail().trim()).append(" уже использован другим пользователем.<br />");
        }

        if(!form.containsKey("UserAgrees")){
            errorsText.append("Необходимо подтвердить согласие на использование сервисаю.<br />");
        }

        if (errorsText.length() != 0) {
            model.put("message", errorsText);       //ошибки все же были...
            return "registration/registration_form";
        }

        // Добавим в базу нового пользователя
        //пошлем ему письмо со ссылкой на активацию
        //если за сутки не активируется - удалим запись о регистрации классом

        // выделим url приложения из контекста request для формирования ссылки в письме !!!!
        String url=request.getRequestURL().toString(); // http://localhost:8000/registration/adduser
        String uri=request.getRequestURI();             // /registration/adduser
        url=url.substring(0,url.indexOf(uri));            // http://localhost:8000

        String result= userService.addUser(user, url);

        switch (result) {
            case "ok": {
                //Ошибок при контроле не было - пользователь успешно добавлен
                 return "redirect:/index";
                //break;
            }
            case "bad email":{
                errorsText.append("Неудачная отправка email с кодом активации. Регистрация не может быть закончена. Попробуйте позже.");
                // логируем ошибку
                log.error(errorsText.toString());
                userService.deleteUser(user);
                model.put("message", errorsText);       //ошибки все же были...
                return "registration/registration_form";

            }
            //Может быть еще будут другие результаты addUser

            default: {
                errorsText.append("Неопределенная ошибка регистрации. Обратитесь в поддержку.");
                log.error(errorsText.toString());
                userService.deleteUser(user);
                model.put("message", errorsText);       //ошибки все же были...
                return "registration/registration_form";
                //break;
            }
        }

    }

    @GetMapping("/activate/{code}")
    //ловит ссылку из email для активации
    public String activate(Map<String, Object> model, @PathVariable String code) {
        model.put("MyApp", new MyAppConst()); //Для header_bs.html и footer.html
        boolean isActivated = userService.activateUser(code);

        if (isActivated) {
            return "redirect:/registration/msglogin/msgActivationIsOk";
        } else {
            return "redirect:/registration/msglogin/msgNotActivated";
        }
    }


}
