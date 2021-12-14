package ru.dmv.lk.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.dmv.lk.model.User;
import ru.dmv.lk.service.UserService;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


/**
 Класс для вызовов из Tabulator.js по ajax.
 @author Булатов О.Б
 @version 1.0
 */
@RestController
@RequestMapping(method = {RequestMethod.GET, RequestMethod.POST})
public class ApiTabulator {

    private final UserService userService;

    @Autowired
    public ApiTabulator(UserService userService) {
        this.userService=userService;

    }

    /**
     Метод возвращает json массив с некоторыми данными Users. Не используется.
     @return Вовращает json массива всех users с полями, необходимы в отображении формы findUserForm
     Напрмер:
     [{"id":1,"username":"admin","email":"info@doma-sarapula.ru","wasActive":null,"stringWasActive":"21.10.19"},
     {"id":2,"username":"apilkuser","email":"info@doma-sarapula.ru","wasActive":null,"stringWasActive":"21.10.19"},
     {"id":5,"username":"primer","email":"info@doma-sarapula.ru","wasActive":null,"stringWasActive":"21.10.19"}]
     */
    @GetMapping(value = "/apitabulator/getJsonUsers", produces ="application/json" )
    public String getAllUsers() {
        Collection<User> users = userService.getAllUsers().stream()
                .sorted(new Comparator<User>(){
                    public  int compare(User o1, User o2) {return o1.getUsername().compareTo(o2.getUsername());}
                })
                .collect(Collectors.toCollection(ArrayList::new));
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); //The module teaches the ObjectMapper how to work with LocalDate
        String resultJson="[]";
        try {
            resultJson=objectMapper.writeValueAsString(users);
        } catch (IOException e) {
            new RuntimeException(e.toString());
        }
        return resultJson;
    }
    /**
     Метод возвращает данные для станицы c Users, вызывается по ajax из findUser.html для получения страницы со списком пользователей
     @param page Номер страницы таблицы, которая запрашиватется
     @param form Мар данных формы
     @param size Размер страницы в строках данных
     @param sorters Текущая сортировка. Не используется
     @return Вовращает json данных для отображения страницы в форме findUserForm
     Например:
     "last_page":15, //the total number of available pages (this value must be greater than 0)
     "data":[ // an array of row data objects
     {"id":1,"username":"admin","email":"info@doma-sarapula.ru","wasActive":null,"stringWasActive":"21.10.19"},
     {"id":2,"username":"apilkuser","email":"info@doma-sarapula.ru","wasActive":null,"stringWasActive":"21.10.19"},{"id":5,"username":"primer","email":"info@doma-sarapula.ru","wasActive":null,"stringWasActive":"21.10.19"}
     ]
     */
    @GetMapping(value = "/apitabulator/getJsonPageOfUsers", produces ="application/json" )
    public String getJsonPageOfUsers(@RequestParam(required = false) String page,
                                     @RequestParam(required = false) String size,
                                     @RequestParam(required = false) String sorters,
                                     @NotNull @RequestParam Map<String, String> form) {
        Collection<User> users = userService.getAllUsers();
        //применм фильтры. username и/или email
        int i=0;
        while(form.containsKey("filters["+i+"][field]")){
            String field=form.get("filters["+i+"][field]");
            String value=form.get("filters["+i+"][value]");
            if ("username".equals(field)) {
                users=users.stream()
                        .filter(user -> user.getUsername().contains(value))
                        .collect(Collectors.toSet());
            }else if ("email".equals(field)) {
                users = users.stream()
                        .filter(user -> user.getEmail().contains(value))
                        .collect(Collectors.toSet());
            }
            i++;
        }


        long pages=users.size()/Long.parseLong(size);
        if(pages*Long.parseLong(size)<users.size()) pages=pages+1; //если деление выше с остатком
        //Выводим сортированным по username
        Comparator<User> comparatorUsers = Comparator.comparing(User::getUsername);
        Collection<User> usersPage = users.stream()
                .sorted(comparatorUsers)
                .skip((Long.parseLong(page)-1)*Long.parseLong(size))
                .limit(Long.parseLong(size))
                .collect(Collectors.toCollection(ArrayList::new));

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); //The module teaches the ObjectMapper how to work with LocalDate

        String response;
        String resultJson="[]";
        try {
            resultJson=objectMapper.writeValueAsString(usersPage);
        } catch (IOException e) {
            new RuntimeException(e.toString());
        }

        response="{"+"\"last_page\":"+ Long.toString(pages) +","+"\"data\":"+resultJson+"}";
        return response;
    }


 
}