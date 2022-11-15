package com.github.nikyotensai.elf.server.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.github.nikyotensai.elf.server.ui",
        "com.github.nikyotensai.elf.server.proxy",
        "com.github.nikyotensai.elf.application.api",
        "com.github.nikyotensai.elf.server"})
public class ElfServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ElfServerApplication.class);
    }
}
