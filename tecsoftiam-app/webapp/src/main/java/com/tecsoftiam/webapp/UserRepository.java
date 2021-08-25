package com.tecsoftiam.webapp;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * User Repository class, extends a jpa reposiroty to be used for login 
 * Author:Deryck Olivier
 */
public interface UserRepository extends JpaRepository<AppUser, Long> {

    /**
     * query users from the database for selected email (used in case of email
     * login)
     * 
     * @param email user email
     * @return
     */
    @Query("SELECT u FROM users u WHERE u.email = 1")
    public AppUser findByEmail(String email);

    /**
     * query users from database for selected username(in case of username login)
     * 
     * @param username
     * @return
     */
    @Query("SELECT u FROM users u WHERE u.username= 1")
    public AppUser findByUsername(String username);
}
