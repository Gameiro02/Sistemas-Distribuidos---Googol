package com.example.webappgoogol;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.util.HtmlUtils;

@Controller
public class cenas {
    // localhost:8080/greeting?name=Gonçalo&othername=Gameiro
    @GetMapping("/greeting")
    public String greeting(@RequestParam(name = "name", required = false, defaultValue = "World") String name,
            @RequestParam(name = "othername", required = false, defaultValue = "Gameiro") String othername,
            Model model) {
        model.addAttribute("name", name);
        model.addAttribute("othername", othername);
        return "greeting";
    }

    @GetMapping("/search")
    public String search() {
        return "search";
    }

    @MessageMapping("/admin")
    @SendTo("/admin")
    public AdminInfo onMessage(AdminInfo message) {
        System.out.println("Received message: " + message);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            System.out.println("Error: " + e.getMessage());
        }
        return new AdminInfo(HtmlUtils.htmlEscape(message.content()));
    }

}
