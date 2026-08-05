package com.ayrotek.userauthenticationservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class MainController {

    /**
     * Herkese açık endpoint.
     * SecurityConfig içinde ".permitAll()" kısmına "/api/v1/welcome" ekleyerek test edebilirsin.
     */
    @GetMapping("/welcome")
    public String allAccess() {
        return "Everyone access";
    }

    /**
     * Token gerektiren standart kullanıcı endpoint'i.
     * Geçerli bir JWT olmadan istek atıldığında 401 Unauthorized dönecektir.
     */
    @GetMapping("/user")
    public String userAccess() {
        return "User Content with JWT";
    }

    /**
     * Token gerektiren özel/korumalı endpoint.
     * Tıpkı /user gibi, bu alana da sadece kimliği doğrulanmış kullanıcılar istek atabilir.
     */
    @GetMapping("/special")
    public String specialAccess() {
        return "Special access with JWT";
    }
}