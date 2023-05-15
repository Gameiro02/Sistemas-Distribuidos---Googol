package com.example.webappgoogol.HackerNewsAPI;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * This class is responsible for getting the top stories and the stories of a given user from Hacker News API. <p>
 */
public class HackerNewsAPI {

    /**
     * Main method for testing purposes
     */
    public static void main(String[] args) {
        HackerNewsAPI hackerNewsAPI = new HackerNewsAPI();
        hackerNewsAPI.getTopStories();
        hackerNewsAPI.getUserStories("lg");
    }

    /**
     * Returns a List object with the urls of the top stories in Hacker News. <p>
     * This method might take a while to run.
     * When it starts running, it will print "Getting top stories..." to the console
     * @return List of Strings containing the urls of the top stories
     */
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

    /**
     * Returns a List object with the urls of the stories of a given user.
     * This method might take a while to run.
     * When it starts running, it will print "Getting 'username' top stories..." to the console.
     * @param username String containing the username of the user
     * @return List of Strings containing the urls of the stories of the user
     */
    public List<String> getUserStories(String username) {
        List<String> userStoryUrls = new ArrayList<String>();

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

            userStoryUrls = jsonParser(contentList);
            for (String string : userStoryUrls) {
                System.out.println(string);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

        return userStoryUrls;
    }

    /**
     * Returns a List object with the URLs of the stories <p>
     * This method might take a while to run <p>
     * Prints all the invalid URLs to the console
     * @param contentList String[] containing the ids of the stories
     * @return List of Strings containing the urls of the stories
     * @throws InterruptedException
     */
    private List<String> jsonParser(String[] contentList) throws InterruptedException {

        List<String> urlList = new ArrayList<String>();
        List<Thread> threads = new ArrayList<Thread>();
        int numThreads = 10; // number of threads to create

        List<String> invalidURLS = new ArrayList<String>();

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
                            invalidURLS.add(storyObject.get("id").toString());
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

        if (invalidURLS.size() > 0) {
            System.out.println("Invalid URLs:");
            System.out.println(invalidURLS.toString());
        }

        return urlList;
    }
}