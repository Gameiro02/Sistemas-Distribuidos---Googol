package com.example.webappgoogol.HackerNewsAPI;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class HackerNewsAPI {
    public static void main(String[] args) {
        HackerNewsAPI hackerNewsAPI = new HackerNewsAPI();
        hackerNewsAPI.getUserTopStories("jl");
    }

    // Returns a list with the 500 top stories urls
    public List<String> getTopStories() {

        List<String> urlList = new ArrayList<String>();

        System.out.println("Getting top stories...");

        try {
            URL url = new URL("https://hacker-news.firebaseio.com/v0/topstories.json?print=pretty");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer content = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }

            in.close();
            con.disconnect();

            String[] contentList = content.toString().split(", ");

            // Remove "[ " and " ]" from first and last element
            contentList[0] = contentList[0].substring(2);
            contentList[contentList.length - 1] = contentList[contentList.length - 1].substring(0,
                    contentList[contentList.length - 1].length() - 2);
            
            urlList = jsonParser(contentList);
            // for (String string : urlList) {
            //     System.out.println(string);
            // }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return urlList;
    }

    private List<String> jsonParser(String[] contentList) throws MalformedURLException, IOException, ProtocolException, ParseException, InterruptedException {

        List<String> urlList = new ArrayList<String>();
        List<Thread> threads = new ArrayList<Thread>();
        int numThreads = 10; // number of threads to create

        for (int i = 0; i < contentList.length; i++) {
            final int index = i;
            Thread t = new Thread(() -> {
                try {
                    URL story = new URL("https://hacker-news.firebaseio.com/v0/item/" + contentList[index] + ".json?print=pretty");
                    HttpURLConnection storyCon = (HttpURLConnection) story.openConnection();
                    storyCon.setRequestMethod("GET");
                    BufferedReader storyIn = new BufferedReader(new InputStreamReader(storyCon.getInputStream()));

                    JSONParser parser = new JSONParser();
                    String storyInputLine = "";
                    StringBuffer storyContent = new StringBuffer();

                    while ((storyInputLine = storyIn.readLine()) != null) {
                        storyContent.append(storyInputLine);
                    }

                    if (storyContent.length() > 0) {
                        JSONObject storyObject = (JSONObject) parser.parse(storyContent.toString());
                        if (storyObject.get("url") == null) {
                            System.out.println("No url for story " + storyObject.get("id"));
                            return;
                        }
                        urlList.add(storyObject.get("url").toString());

                    } else {
                        System.out.println("No story content");
                    }

                    storyIn.close();
                    storyCon.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            threads.add(t);
            t.start();

            // limit the number of threads to avoid overloading the system
            if (threads.size() == numThreads) {
                for (Thread thread : threads) {
                    thread.join(); // wait for each thread to finish
                }
                threads.clear();
            }
        }

        // wait for the remaining threads to finish
        for (Thread thread : threads) {
            thread.join();
        }

        return urlList;
    }

    // Returns a list with the urls of the stories of a given user
    public List<String> getUserTopStories(String username) {
        List<String> urlList = new ArrayList<String>();

        System.out.println("Getting @" + username + " top stories...");

        try {
            URL url = new URL("https://hacker-news.firebaseio.com/v0/user/" + username + ".json?print=pretty");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer content = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }

            in.close();
            con.disconnect();

            JSONParser parser = new JSONParser();
            JSONObject userObject = (JSONObject) parser.parse(content.toString());

            String[] contentList = userObject.get("submitted").toString().split(",");

            contentList[0] = contentList[0].substring(1);
            contentList[contentList.length - 1] = contentList[contentList.length - 1].substring(0,
                    contentList[contentList.length - 1].length() - 1);

            urlList = jsonParser(contentList);
            for (String string : urlList) {
                System.out.println(string);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return urlList;
    }
}