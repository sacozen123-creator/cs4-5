package com.gym.config;

import com.gym.model.User;
import com.gym.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements ApplicationRunner {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // Check if admin already exists
        User existingAdmin = userRepo.findByUsername("admin");
        
        if (existingAdmin == null) {
            // Create admin user
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole("ROLE_ADMIN");
            admin.setFullName("System Administrator");
            admin.setEmail("admin@gymsystem.com");
            admin.setUserType("ADMIN");
            admin.setJoinedDate(java.time.LocalDate.now().toString());
            
            userRepo.save(admin);
            System.out.println("========================================");
            System.out.println("✅ Admin account created!");
            System.out.println("   Username: admin");
            System.out.println("   Password: admin123");
            System.out.println("========================================");
        } else {
            System.out.println("✅ Admin account already exists - Username: admin, Password: admin123");
        }
    }
}