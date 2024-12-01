package io.github.pengxianggui.crud.demo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("io.github.pengxianggui.crud.demo.mapper")
@SpringBootApplication(scanBasePackages = {"io.github.pengxianggui.crud.demo", "io.github.pengxianggui.crud"}) // TODO fast-crud-spring-boot-starter
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
