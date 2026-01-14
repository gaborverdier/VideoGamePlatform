package com.vgp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PlatformServiceApplication {

    public String getGreeting() {
        return "\n#########################\nPlatform Service Started!\n#########################\n";
    }
    
    public static void main(String[] args) {
        SpringApplication.run(PlatformServiceApplication.class, args);
    }
}
