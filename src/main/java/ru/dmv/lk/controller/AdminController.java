package ru.dmv.lk.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.dmv.lk.dto.MyAppConst;
import ru.dmv.lk.model.Role;
import ru.dmv.lk.model.User;
import ru.dmv.lk.service.UserService;
import ru.dmv.lk.utils.MyUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
/*@PreAuthorize("hasAuthority('ADMIN')")*/
public class AdminController {

    @Value("${logging.file.name}")
    private String loggingFileName;

    @Autowired
    private UserService userService;
    @Autowired
    private MyUtil myUtil;

    private static Logger log = LoggerFactory.getLogger(AdminController.class);



    @GetMapping(value = {"/kill_application"})
    public String kill_application(){
        System.exit(0); // Это слишком жестко.... НО в нашем случее вполне устраивает
        return "/";
    }

    @GetMapping("/userslist")
    public String userList(Model model) { return "/admin/admin";}

    @GetMapping("/findUser")
    public String findUserForEditRole (Map<String, Object> model,
                                       HttpServletRequest request) throws Exception{
        model.put("MyApp", new MyAppConst()); //Для header_bs.html и footer_bs.html
        model.put("whatDo","editRole");
         return ("admin/findUser");
    }

    /**
     * Выбирает пользователя, фомирует данные для вызываемого Html редактирования данных пользователя
     * @param model - Map модели из формы, для удобства возврата данных в форму
     * @param user_id - User.getId() в String
     * @return вызов admin/editUserForm.hml
     */
    @GetMapping("/editUser")
    public String editUser(Map<String, Object> model,
                                @RequestParam(value = "id", required = true) String user_id){
        model.put("MyApp", new MyAppConst()); //Для header_bs.html и footer.html
        User user = userService.getById(Long.parseLong(user_id));
        if (user != null){
            Map <Role,Boolean> rolesMap = this.getMapRolesFromUser(user);
            model.put("rolesMap",rolesMap) ;
            model.put("user", user);    // в модель весь объект
            return "admin/editUserForm";
        }else{
            model.put("error_noUser", true);
            return "admin/findUser";
        }
    }


    /**
     * по submit из admin/editUserRoleSave.html
     * Сохраняем отредактировнные данные
     * @param model -Map модели из формы, для удобства возврата данных в форму
     * @param username - User.getUsername()
     * @param form - Map данных из формы, вызвавшей метод
     * @return вызов admin/findUser.html
     */
    @PostMapping("/editUserSave")
    @Transactional
    // здесь хороший пример получнения списка form и username из формы на браузере
    public String editedRolesSave(Map<String, Object> model,
                                  @RequestParam String username,
                                  @RequestParam Map<String, String> form) {
        model.put("MyApp", new MyAppConst()); //Для header_bs.html и footer.html
        if (form.containsKey("username")) { //на форме есть поле с наименованием?
            User user = userService.getUserByUsername(username);
            if (user != null) {
                if(!form.containsKey("cancel")){

                    user.setActive(form.containsKey("active"));

                    Set<String> roles =
                            Arrays.stream(Role.values())
                                    .map(Role::name)
                                    .collect(Collectors.toSet());
                    // .map(Role::name) - вызов автоматически предопределённый метод для перечисления name() в синтаксисе лямбда
                    //Метод name() возвращает имя точно в таком виде, в каком оно было объявлено;
                    // эту же информацию возвращает метод toString()
                    user.getRoles().clear();


                    for (String key : form.keySet()) {
                        if (roles.contains(key)) {
                            user.getRoles().add(Role.valueOf(key));
                        }
                        if(key.equals("newPassword") & !form.get(key).isEmpty()){
                            userService.setPassword(user,form.get(key).trim());
                            log.info("Для пользователя "+username+" установлен пароль "+form.get(key).trim());
                        }
                    }
                    log.info("Для пользователя "+username+" установлены роли "+user.getRoles());

                    if(form.containsKey("delete")) {
                        userService.deleteUser(user);
                        log.info("Пользователь "+username+" удален");
                    }
                    else  userService.saveUser(user);

                } //if(!form.containsKey("cancel"))
                return ("admin/findUser");
            }else{
                log.info("Не нашли пользователя "+username+" для изменения");
                return ("admin/findUser"); }
        } else {
            log.info("На template /admin/editUserForm нет поля username");
            return ("index");
        }

    }

    /**
     * Возвращает map список всех ролей, возможных  для User с отметкой о применении для конкретного user
     * @param user - User
     * @return Map<Role, Boolean> содержащий список всех ролей, возможных  для User
     * с отметкой о применении для конкретного user
     */
    private Map<Role,Boolean> getMapRolesFromUser(User user) {
        Map<Role, Boolean> rolesMap = new HashMap();
        // положим в Map все роли с false
        Role[] roles = Role.values(); // [ROLE_USER, ROLE_ADMIN]
        for (Role role : roles) {
            rolesMap.put(role, false);
        }
        for (Map.Entry<Role, Boolean> roleEntry : rolesMap.entrySet()) {
            if (user.getRoles().contains(roleEntry.getKey())) {
                roleEntry.setValue(true);
            } else {
                roleEntry.setValue(false);
            }
        }
        return rolesMap;
    }

    @GetMapping("/downloadLogFile")
    public void dowloadLogFile(Map<String, Object> model,HttpServletResponse response) throws Exception{
        //кривое решение по Exception...
            myUtil.downloadFile(loggingFileName,response);
    }

}
