package com.example.webappgoogol;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.example.webappgoogol.SearchModule.SearchModuleInterface;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.example.webappgoogol.HackerNewsAPI.HackerNewsAPI;

@Controller
public class GoogolController {
    private SearchModuleInterface searchModule;
    private HackerNewsAPI hackerNewsAPI;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    public GoogolController(SearchModuleInterface searchModule) {
        this.searchModule = searchModule;
        this.hackerNewsAPI = new HackerNewsAPI();
    }

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
            Model model) throws Exception {

        System.out.println(query);

        if (query.equals("")) {
            return "search";
        }

        try {
            List<String> results = searchModule.searchForWords(query);
            model.addAttribute("results", results);
        } catch (Exception e) {
            System.out.println("Erro ao conectar com o servidor!!!!!!!");
        }

        // Envie a mensagem para os clientes conectados ao tópico "/topic/admin"
        messagingTemplate.convertAndSend("/topic/admin", new Mensagem(convertToJSON(searchModule.getStringMenu())));

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
            System.out.println("Error: " + e.getMessage());
        }

        return "IndexHackersByUsername";
    }

    @GetMapping("/IndexHackersNews")
    public String IndexHackersNews(Model model) {
        List<String> results = new ArrayList<String>();

        model.addAttribute("results", "A indexar os top stories do Hacker News");

        try {
            results = hackerNewsAPI.getTopStories();

            if (results.isEmpty() || results == null) {
                model.addAttribute("results", "Erro a ir buscar os top stories: A lista vem vazia ou esta null");
                return "search";
            }

            for (String url : results) {
                boolean searching = true;
                while (searching) {
                    try {
                        searchModule.IndexarUmNovoUrl(url);
                        searching = false;
                    } catch (Exception e) {
                        searching = true;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            model.addAttribute("results", "Ocorreu um erro ao indexar os top stories do Hacker News");
            return "error";
        }

        model.addAttribute("results", "Top stories do Hacker News indexados com sucesso!");
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

        String s = convertToJSON(searchModule.getStringMenu());

        printJSON(s);

        return new Mensagem(s);
    }

    @GetMapping("/")
    public String root() {
        return "menu";
    }

    @GetMapping("/register")
    public String register(Model model) {
        String username = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest()
                .getParameter("username");
        String password = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest()
                .getParameter("password");

        if (username == null || password == null) {
            return "register";
        }

        File file = new File("src\\main\\java\\com\\example\\webappgoogol\\login.txt");

        try {
            file.createNewFile();
        } catch (IOException e) {
            System.out.println("Error creating login file: " + e.getMessage());
        }

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(";");

                if (parts[0].equals(username)) {
                    model.addAttribute("results", "Username já existe!");
                    return "register";
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Error reading login file: " + e.getMessage());
        }

        try (FileWriter fileWriter = new FileWriter(file, true);
                BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                PrintWriter out = new PrintWriter(bufferedWriter)) {
            out.println(username + ";" + password);
        } catch (IOException e) {
            System.out.println("Error writing to login file: " + e.getMessage());
        }

        return "register";
    }

    @GetMapping("/login")
    public String processLogin(Model model) {
        String username = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest()
                .getParameter("username");
        String password = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest()
                .getParameter("password");

        if (username == null || password == null) {
            return "login";
        }

        File file = new File("src\\main\\java\\com\\example\\webappgoogol\\login.txt");

        try {
            file.createNewFile();
        } catch (IOException e) {
            System.out.println("Error creating login file: " + e.getMessage());
        }

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(";");
                if (parts[0].equals(username) && parts[1].equals(password)) {
                    System.out.println("Login successful!");
                    return "redirect:/search";
                }
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            System.out.println("Error: " + e.getMessage());
        }

        System.out.println("Login failed!");
        return "redirect:/search";
    }

    public static void printJSON(String json) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Object jsonObject = objectMapper.readValue(json, Object.class);
            String prettyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonObject);
            System.out.println(prettyJson);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String convertToJSON(String input) {
        List<String> downloaders = new ArrayList<>();
        List<String> barrels = new ArrayList<>();
        List<String> searches = new ArrayList<>();

        // Parse the input string and extract the relevant information
        String[] lines = input.split("\n");
        int state = 0; // 0 - Downloaders, 1 - Barrels, 2 - Most Frequent Searches

        for (String line : lines) {
            if (line.startsWith("------- Downloaders -------")) {
                state = 0;
            } else if (line.startsWith("------- Barrels -------")) {
                state = 1;
            } else if (line.startsWith("------- Most Frequent Searches -------")) {
                state = 2;
            } else if (!line.isEmpty()) {
                switch (state) {
                    case 0:
                        downloaders.add(line);
                        break;
                    case 1:
                        barrels.add(line);
                        break;
                    case 2:
                        searches.add(line);
                        break;
                }
            }
        }

        // Create the JSON object using Jackson
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode json = objectMapper.createObjectNode();

        // Add the downloader information
        json.put("num_downloaders", downloaders.size());
        ArrayNode downloaderStates = objectMapper.createArrayNode();
        for (String downloader : downloaders) {
            downloaderStates.add(downloader);
        }
        json.set("downloader_states", downloaderStates);

        // Add the barrel information
        json.put("num_barrels", barrels.size());
        ArrayNode barrelStates = objectMapper.createArrayNode();
        for (String barrel : barrels) {
            barrelStates.add(barrel);
        }
        json.set("barrel_states", barrelStates);

        // Add the search information
        json.put("num_searches", searches.size());
        ArrayNode searchStates = objectMapper.createArrayNode();
        for (String search : searches) {
            searchStates.add(search);
        }
        json.set("search_states", searchStates);

        // Convert the JSON object to a string
        try {
            return objectMapper.writeValueAsString(json);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

}
