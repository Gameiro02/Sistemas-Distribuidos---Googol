package com.example.webappgoogol;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.HtmlUtils;

import java.util.*;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.io.*;
import java.net.MalformedURLException;

import com.example.webappgoogol.SearchModule.SearchModuleInterface;

@Controller
public class GoogolController {
    private SearchModuleInterface searchModule;

    @Autowired
    public GoogolController(SearchModuleInterface searchModule) {
        this.searchModule = searchModule;
    }

    // localhost:8080/greeting?name=Gonçalo&othername=Gameiro
    @GetMapping("/greeting")
    public String greeting(@RequestParam(name = "name", required = false, defaultValue = "World") String name,
            @RequestParam(name = "othername", required = false, defaultValue = "Gameiro") String othername,
            Model model) {
        model.addAttribute("name", name);
        model.addAttribute("othername", othername);
        return "greeting";
    }

    // When the button is pressed, the form is submitted and we print the result in
    // the ${query} variable
    @GetMapping("/search")
    public String search(@RequestParam(name = "query", required = false, defaultValue = "") String query,
            Model model) {

        System.out.println(query);

        try {
            List<String> results = searchModule.searchForWords(query);
            model.addAttribute("results", results);
        } catch (Exception e) {
            System.out.println("Erro ao conectar com o servidor!!!!!!!");
            try {
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }

        return "search";
    }

    @GetMapping("/indexNewUrl")
    public String indexNewUrl(@RequestParam(name = "url", required = false, defaultValue = "") String url,
            Model model) {

        if (url.equals("")) {
            return "indexNewUrl";
        }
        System.out.println("url = " + url);

        try {
            searchModule.IndexarUmNovoUrl(url);
            String message = "Url indexada com sucesso!";
            model.addAttribute("results", message);
        } catch (Exception e) {
            System.out.println("Erro ao conectar com o servidor!!!!!!!");
            try {
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }

        return "indexNewUrl";
    }

    @GetMapping("/listPages")
    public String listPages(@RequestParam(name = "url", required = false, defaultValue = "") String url, Model model) {

        System.out.println("url to list = " + url);

        try {
            List<String> results = searchModule.linksToAPage(url);
            System.out.println("results = " + results);
            model.addAttribute("results", results);
        } catch (Exception e) {
            System.out.println("Erro ao conectar com o servidor!!!!!!!");
            try {
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }

        return "listPages";
    }

    // redirect the root to the search page
    @GetMapping("/")
    public String root() {
        return "redirect:/search";
    }

    @GetMapping("/admin")
    public String admin() {
        return "admin";
    }

    // /admin = /topic
    @MessageMapping("/update")
    @SendTo("/admin/updates")
    public AdminInfo onMessage(AdminInfo adminInfo) {
        System.out.println("AdminInfo: " + adminInfo);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            System.out.println("Error: " + e.getMessage());
        }
        return new AdminInfo(HtmlUtils.htmlEscape(adminInfo.content()));
    }

}
