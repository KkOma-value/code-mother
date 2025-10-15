package com.example.codemother;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.mybatis.spring.annotation.MapperScan;



@EnableCaching
@SpringBootApplication
@MapperScan("com.example.codemother.mapper")
public class CodeMotherApplication {

    public static void main(String[] args) {
        SpringApplication.run(CodeMotherApplication.class, args);
    }

}
