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
 * This class is responsible for getting the top stories and the stories of a
 * given user from Hacker News API.
 * <p>
 */
public class HackerNewsAPI {
    /**
     * Returns a List object with the urls of the top stories in Hacker News.
     * <p>
     * This method might take a while to run.
     * When it starts running, it will print "Getting top stories..." to the console
     * 
     * @return List of Strings containing the urls of the top stories
     */
    public List<String> getTopStories() {

        List<String> urlList = new ArrayList<String>(); // List of Strings containing the urls of the top stories
        System.out.println("Getting top stories...");

        try {
            URL url = new URL("https://hacker-news.firebaseio.com/v0/topstories.json?print=pretty"); // URL that returns the top stories

            // Open the connection
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            // Read the response from the API
            for (int i = 0; i < 10; i++) {
                inputLine = in.readLine();
                response.append(inputLine);
            }
            
            // Close the connection
            in.close();
            connection.disconnect();

            String[] contentList = response.toString().split(", ");

            // Remove "[ " and " ]" from first and last element
            contentList[0] = contentList[0].substring(2);
            contentList[contentList.length - 1] = contentList[contentList.length - 1].substring(0,
                    contentList[contentList.length - 1].length() - 2);

            // Parse the JSON response
            urlList = jsonParser(contentList);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return urlList;
    }

    /**
     * Returns a List object with the urls of the stories of a given user.
     * This method might take a while to run.
     * When it starts running, it will print "Getting 'username' top stories..." to
     * the console.
     * 
     * @param username String containing the username of the user
     * @return List of Strings containing the urls of the stories of the user
     */
    public List<String> getUserStories(String username) {

        List<String> userStoryUrls = new ArrayList<String>(); // List of Strings containing the urls of the stories of the user
        System.out.println("Getting @" + username + " top stories...");

        try {
            URL url = new URL("https://hacker-news.firebaseio.com/v0/user/" + username + ".json?print=pretty"); // URL that returns the stories of the user

            // Open the connection
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            // Read the response from the API
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }

            if (response.toString().equals("null")) {
                System.out.println("Error: Hacker News API is not responding");
                return null;
            }

            // Close the connection
            in.close();
            connection.disconnect();

            // Parse the JSON response
            JSONParser parser = new JSONParser();
            JSONObject userObject = (JSONObject) parser.parse(response.toString());

            if (userObject == null) {
                System.out.println("User not found");
                return null;
            }

            // Get the stories ids
            String[] contentList = userObject.get("submitted").toString().split(",");

            // Remove "[" and "]" from first and last element
            contentList[0] = contentList[0].substring(1);
            contentList[contentList.length - 1] = contentList[contentList.length - 1].substring(0,
                    contentList[contentList.length - 1].length() - 1);

            // Parse the JSON response
            userStoryUrls = jsonParser(contentList);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return userStoryUrls;
    }

    /**
     * Returns a List object with the URLs of the stories
     * <p>
     * This method might take a while to run
     * <p>
     * Prints all the invalid URLs to the console
     * 
     * @param contentList String[] containing the ids of the stories
     * @return List of Strings containing the urls of the stories
     * @throws InterruptedException
     */
    private List<String> jsonParser(String[] contentList) throws InterruptedException {

        List<String> urlList = new ArrayList<String>(); // List of Strings containing the urls of the stories
        List<Thread> threads = new ArrayList<Thread>(); // List of Threads
        int numThreads = 10; // Number of threads to create

        List<String> invalidURLS = new ArrayList<String>(); // List of Strings containing stories with invalid URLs

        for (int i = 0; i < 10; i++) {
            final int index = i;

            Thread t = new Thread(() -> {
                try {
                    URL story = new URL("https://hacker-news.firebaseio.com/v0/item/" + contentList[index] + ".json?print=pretty"); // URL that returns the story

                    // Open the connection
                    HttpURLConnection storyCon = (HttpURLConnection) story.openConnection();
                    storyCon.setRequestMethod("GET");

                    BufferedReader storyIn = new BufferedReader(new InputStreamReader(storyCon.getInputStream()));

                    JSONParser parser = new JSONParser();
                    String storyInputLine = "";
                    StringBuffer storyContent = new StringBuffer();

                    // Read the response from the API
                    while ((storyInputLine = storyIn.readLine()) != null) {
                        storyContent.append(storyInputLine);
                    }

                    // Parse the JSON response
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

                    // Close the connection
                    storyIn.close();
                    storyCon.disconnect();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            threads.add(t);
            t.start();

            // Limit the number of threads to avoid overloading the system
            if (threads.size() == numThreads) {
                for (Thread thread : threads) {
                    thread.join(); // Wait for each thread to finish
                }
                threads.clear();
            }
        }

        // Wait for the remaining threads to finish
        for (Thread thread : threads) {
            thread.join();
        }

        // Print the invalid URLs
        if (invalidURLS.size() > 0) {
            System.out.println("Invalid URLs:");
            System.out.println(invalidURLS.toString());
        }

        return urlList;
    }
}