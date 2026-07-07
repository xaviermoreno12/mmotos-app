package com.mmotos.infrastructure.input.rest;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final ConfigurableApplicationContext context;

    public AdminController(ConfigurableApplicationContext context) {
        this.context = context;
    }

    @PostMapping("/apagar")
    @PreAuthorize("hasRole('DUENO')")
    public Map<String, String> apagar() {
        new Thread(() -> {
            try { Thread.sleep(500); } catch (InterruptedException ignored) {}
            SpringApplication.exit(context, () -> 0);
        }).start();
        return Map.of("mensaje", "Servidor apagándose...");
    }
}
