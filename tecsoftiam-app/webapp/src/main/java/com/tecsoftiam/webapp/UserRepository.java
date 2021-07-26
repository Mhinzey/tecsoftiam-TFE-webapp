package com.tecsoftiam.webapp;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository  extends JpaRepository<AppUser, Long>{

    @Query("SELECT u FROM users u WHERE u.email = 1")
    public AppUser findByEmail(String email);
    
    @Query("SELECT u FROM users u WHERE u.username= 1")
    public AppUser findByUsername(String username);
}
