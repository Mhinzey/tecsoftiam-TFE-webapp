package com.tecsoftiam.webapp;

import static org.assertj.core.api.Assertions.assertThat;
import com.tecsoftiam.webapp.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.annotation.Rollback;
 
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Rollback(false)
public class UserRepositoryTests {
 
    @Autowired
    private TestEntityManager entityManager;
     
    @Autowired
    private UserRepository repo;
    @Test
    public void testCreateUser() {
        AppUser user = new AppUser();
        user.setEmail("ravikumar@gmail.com");
        user.setPassword("ravi2020");
        user.setUsername("Ravi");
         
        AppUser savedUser = repo.save(user);
         
        AppUser existUser = entityManager.find(AppUser.class, savedUser.getId());
         
        assertThat(user.getEmail()).isEqualTo(existUser.getEmail());
         
    }
    // test methods go below
}