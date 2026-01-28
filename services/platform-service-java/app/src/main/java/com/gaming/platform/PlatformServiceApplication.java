package com.gaming.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling
public class PlatformServiceApplication {
    public String getGreeting() {
        return "\n#########################\nPlatform Service Started (client side)\n#########################\n";
    }

    public static void main(String[] args) {
        SpringApplication.run(PlatformServiceApplication.class, args);
        System.out.println(new PlatformServiceApplication().getGreeting());
    }
}
