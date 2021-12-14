package ru.dmv.lk.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import ru.dmv.lk.model.Role;
import ru.dmv.lk.model.User;
import ru.dmv.lk.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface UserService extends UserDetailsService {

    UserDetails loadUserByUsername(String username);
    boolean isUserName (User user );
    boolean isUserEmail (User user );
    String addUser(User user, String url);
    void deleteUser(User user);
    boolean activateUser(String code);
    User getUserByUsername(String username); // это получение без лишнего кода
    void setPassword(User user, String password);
    void saveUser(User user);
    ArrayList<User> deleteUsersWithOverdueActivationCode(int howManyDaysAfterRegistration);
    void addNewUserPrgWay(String username, String password, Role role);
    void addUsefullUsers();
    String makeEmail(User user,  Map<String,Object> model, Map<String, String> form ) throws Exception;
    List<User> getAllUsers();
    User getById(Long id);
    Set<User> findByEmail(String email);

}
