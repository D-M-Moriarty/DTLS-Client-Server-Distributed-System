package com.darren.ca.server.database;

import com.darren.ca.server.model.User;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface UserRepository extends CrudRepository<User, String> {
    List<User> findUserByUsernameAndPassword(String username, String password);

    List<User> findByUsername(String username);
}
