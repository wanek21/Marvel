package com.marvel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableSwagger2
public class MainApplication {

    public static void main(String[] argc) {
        SpringApplication.run(MainApplication.class, argc);
    }
}
