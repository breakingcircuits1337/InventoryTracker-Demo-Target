package com.acme.inventory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot entry point — replaces web.xml + applicationContext.xml + dispatcher-servlet.xml.
 * L2L[TRANSFORM]: Framework migration Spring MVC 4.3 WAR → Spring Boot 3.2 JAR.
 */
@SpringBootApplication
public class InventoryTrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(InventoryTrackerApplication.class, args);
    }
}
