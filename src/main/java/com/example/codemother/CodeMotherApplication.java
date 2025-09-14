package com.example.codemother;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
//添加扫描文件
@MapperScan("com.example.codemother.mapper")
@ComponentScan({"com.example.codemother", "core"})
public class CodeMotherApplication {

    public static void main(String[] args) {
        SpringApplication.run(CodeMotherApplication.class, args);
    }

}
