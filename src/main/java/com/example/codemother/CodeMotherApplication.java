package com.example.codemother;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
//添加扫描文件
@MapperScan("com.example.codemother.mapper")
public class CodeMotherApplication {

    public static void main(String[] args) {
        SpringApplication.run(CodeMotherApplication.class, args);
    }

}
