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
import com.example.webappgoogol.HackerNewsAPI.HackerNewsAPI;

@Controller
public class GoogolController {
    private SearchModuleInterface searchModule;
    private HackerNewsAPI hackerNewsAPI;

    @Autowired
    public GoogolController(SearchModuleInterface searchModule) {
        this.searchModule = searchModule;
        this.hackerNewsAPI = new HackerNewsAPI();
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

        if (query.equals("")) {
            return "search";
        }

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

    @GetMapping("/IndexHackersByUsername")
    public String IndexHackersByUsername(
            @RequestParam(name = "username", required = false, defaultValue = "") String username, Model model) {
        List<String> results = new ArrayList<String>();
        try {
            results = hackerNewsAPI.getUserStories(username);

            if (results.size() == 0) {
                model.addAttribute("results", "O utilizador não existe ou não tem histórias!");
                return "IndexHackersByUsername";
            }

            model.addAttribute("results", results);
            System.out.println("results = " + results);
            for (String url : results) {
                searchModule.IndexarUmNovoUrl(url);
            }
        } catch (Exception e) {
            System.out.println("Erro ao conectar com o servidor!!!!!!!");
            try {
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
        return "IndexHackersByUsername";
    }

    @GetMapping("/IndexHackersNews")
    public String IndexHackersNews(Model model) {
        List<String> results = new ArrayList<String>();

        if (results.isEmpty() || results == null) {
            model.addAttribute("results", "Erro a ir buscar os top stories: A lista vem vazia ou esta null");
            return "search";
        }

        model.addAttribute("results", "A indexar os top stories do Hacker News");
        try {
            results = hackerNewsAPI.getTopStories();
            for (String url : results) {
                searchModule.IndexarUmNovoUrl(url);
            }
        } catch (Exception e) {
            System.out.println("Erro ao conectar com o servidor!!!!!!!");
            try {
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
        model.addAttribute("results", "Correu tudo bem");
        return "search";
    }

    @GetMapping("/socketsss")
    public String socketsss() {
        return "seila";
    }

    @MessageMapping("/hello")
    @SendTo("/topic/admin")
    public Mensagem greeting() throws Exception {
        Thread.sleep(1000); // simulated delay
        return new Mensagem(searchModule.getStringMenu());
    }

    @GetMapping("/")
    public String root() {
        return "menu";
    }

    @GetMapping("/teste")
    public String teste() {
        return "teste";
    }

}
