package ru.dmv.lk.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;
import ru.dmv.lk.dto.MyAppConst;
import ru.dmv.lk.model.Role;
import ru.dmv.lk.model.User;
import ru.dmv.lk.repos.UserRepo;
import ru.dmv.lk.utils.MyDate;

import java.time.LocalDate;
import java.util.*;

@Service
public class UserServiceImp implements UserService {

    @Value("${app.email_for_admin}")
    private String appEmailAdmin;
    @Value("${app.password_for_admin}")
    private String appPasswordForAdmin;
    @Value("${app.api.password}")
    private String appApiPassword;
    @Value("${app.api.username}")
    private String appApiUsername;


    private final PasswordEncoder passwordEncoder;
    private final UserRepo userRepo;
    private final EmailServiceImp mailSender;
    private final MyDate myDate;
    private SpringTemplateEngine templateEngine;

    @Autowired
    public UserServiceImp(PasswordEncoder passwordEncoder,
                          UserRepo userRepo,
                          EmailServiceImp mailSender,
                          MyDate myDate ,
                          SpringTemplateEngine templateEngine){
        this.userRepo=userRepo;
        this.passwordEncoder=passwordEncoder;
        this.mailSender=mailSender;
        this.myDate=myDate;
        this.templateEngine=templateEngine;
    }

    private final Logger log = LoggerFactory.getLogger(UserServiceImp.class);


    @Override
    public User getById(Long id) {
        return userRepo.getById(id);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user =    userRepo.findByUsername(username);
        if (user != null) {
            user.setWasActive(LocalDate.now()); // отмечаем, что он сегодня регистрировался
            this.saveUser(user);
        }
        return user;

    }

    @Override  // есть ли User в БД
    public boolean isUserName (User user ){
        // проверяем наличие пользователя по имени
        // Нужно гарантировать, что вернется всегда одно значение или null
        // иначе получим Exception
        User userFromDb = userRepo.findByUsername(user.getUsername());
        return userFromDb != null;
    }

    @Override // есть ли email у User
    public boolean isUserEmail (User user ){
        // проверяем наличие Email в базе
        // может вернуться более 1 записи или null
        Set<User> users = userRepo.findByEmail(user.getEmail());
        return users.size() != 0;
    }

    @Override // Добавить пользователя и послать ему письмо активации
    public String addUser(User user, String url) {

        //добавляем пользователя только после того
        //как проверили все данные для регистрации
        //кодируем пароль passwordEncoder описанный в WebSecurityConfig
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setActive(false); //Поскольку еще не подтвердил email
        user.setRoles(Collections.singleton(Role.ROLE_USER));
        user.setActivationCode(UUID.randomUUID().toString());
        user.setWasActive(LocalDate.now()); // записываем сегодня


        if (!(user.getEmail()==null && user.getEmail().equals(""))) {
            Map<String, Object> varsContext = new LinkedHashMap<>();
            varsContext.put("userName", user.getUsername());
            varsContext.put("activateUrl", url + "/registration/activate/" + user.getActivationCode());
            varsContext.put("MyApp",new MyAppConst());


            try {
                //Загружаем контекст для Thymeleaf из Map
                //где ключ имя переменной для шаблона, а значение - объект любого типа,
                // Например context.setVariable("subscriptionDate", new Date());
                final Context contextThymeleaf = new Context(Locale.ROOT);
                contextThymeleaf.setVariables(varsContext);
                //ThymeLeaf обрабатывает Template-шаблон
                String htmlContent = templateEngine.process("mails/activateLetter.html", contextThymeleaf);

                mailSender.send( user.getEmail(),"Активация e-mail", htmlContent, true);
            }catch(Exception e) {
                return "bad email";
            }

        }

        this.saveUser(user);

        return "ok";
    }

    @Override // удалить пользователя из БД
    public void deleteUser(User user){
        userRepo.deleteById(user.getId());

    }

    @Override // активировать пользоватея по коду активации
    public boolean activateUser(String code) {
        User user = userRepo.findByActivationCode(code);

        if (user == null) {
            return false;
        }
        user.setActive(true);

        user.setActivationCode(null);

        this.saveUser(user);

        return true;
    }

    @Override //    записать User в БД
    public void saveUser(User user) {userRepo.save(user); userRepo.flush();}

    @Override // получить User из БД
    public User getUserByUsername(String username) throws UsernameNotFoundException {
        return userRepo.findByUsername(username);
    }

    @Override // установить пароль в User
    public void setPassword(User user, String password){
        user.setPassword(passwordEncoder.encode(password));
    }

    @Override       //для всех неактивированных User удалить, если прошло howManyDaysAfterRegistration
    //с момена регистрациии
    public ArrayList<User> deleteUsersWithOverdueActivationCode(int howManyDaysAfterRegistration){
        ArrayList<User> users = userRepo.findAllByActiveFalse();
        ArrayList<User> deletedUsers= new ArrayList<>();
        LocalDate dateForDelete=LocalDate.now().minusDays(howManyDaysAfterRegistration);
        for (User user: users) {
            if (user.getWasActive().isBefore(dateForDelete)){
                deletedUsers.add(user);
                this.deleteUser(user);
            }
        }
        return deletedUsers;
    }

    @Override  //создадим пользователя
    public void addNewUserPrgWay(String username, String password, Role role){
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setActive(true);
        user.setRoles(Collections.singleton(role));
        user.setEmail(appEmailAdmin);
        user.setWasActive(this.myDate.today());
        this.saveUser(user);
    }

    @SuppressWarnings("SpellCheckingInspection")
    @Override //Добавляем user admin и primer с данными для показа
    public void addUsefullUsers(){

        //Необходимо добавить Пользователя admin с правами Role.ROLE_ADMIN, если его нет
        User userInDB=this.getUserByUsername("admin");
        if ( userInDB == null) {
            this.addNewUserPrgWay("admin", appPasswordForAdmin, Role.ROLE_ADMIN);
            log.info("Создан пользователь admin");
        }

        //для отработки api
        userInDB=this.getUserByUsername("apiuser");
        if ( userInDB == null) {
            this.addNewUserPrgWay(appApiUsername, appApiPassword, Role.ROLE_API_USER);
            log.info("Создан пользователь apilkuser");
        }

        userInDB = this.getUserByUsername("primer");
        if (userInDB ==null) {
            this.addNewUserPrgWay("primer", "primer", Role.ROLE_USER);
            log.info("Создан пользователь primer");
        }


/*        File customDir = new File(uploadLogoPath);  //делаем раздел для logo файлов за пределом проекта
        if (!customDir.exists()) {
            //noinspection ResultOfMethodCallIgnored
            customDir.mkdir();
        }*/
    }

    @Override //готовим модель для write_email.html - отправить email
    public String makeEmail(User user, Map<String,Object> model, Map<String, String> form) throws Exception{
        model.put("From",user.getEmail());
        model.put("To", appEmailAdmin);
        if (model.get("Function").equals("make")){
            model.put("Subject","");
            model.put("Text","");
        }
        if (model.get("Function").equals("sent")){
            try {
                mailSender.send(user.getEmail(), form.get("Subject"), form.get("Text"), false);
            }catch (Exception e){
                throw new Exception();
            }

            model.put("message","Ваше письмо отправлено.");
            model.put("Subject",form.get("Subject"));
            model.put("Text",form.get("Text"));
        }
        return "user/write_email";
    }

    @Override
    public List<User> getAllUsers() {
        return userRepo.findAll();
    }

    @Override
    public Set<User> findByEmail(String email) {
        return userRepo.findByEmail(email);
    }

}