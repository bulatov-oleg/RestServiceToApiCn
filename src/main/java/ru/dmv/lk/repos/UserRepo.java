package ru.dmv.lk.repos;

import ru.dmv.lk.model.User;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public interface UserRepo extends JpaRepository<User, Long> {
    User findByUsername(String username);
    Set<User> findByEmail(String email);
    User findByActivationCode(String activationCode);
    void deleteById(Long id);
    User save(User user);
    ArrayList<User> findAllByActiveFalse();
    @Override
    List<User> findAll();
    User getById(Long id);
}
