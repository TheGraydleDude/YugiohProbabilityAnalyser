package com.example.yugiohprobabilityanalyser;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();
    }

    /*
    pre:  File name of the ydk we are trying to access
    post: ArrayList of HashMap of Cards and Integers, with the Cards as the key, containing main, side and extra decks.
          We use a HashMap as we do not know the exact size of the deck, and we want to also store the quantity of each card.
          The hash code the Cards is just the hashCode of its id, so the cards should be evenly spaced.
          Even though the outer ArrayList will be constant, java does not allow you to have
          an array of HashMaps, and the size of the outer ArrayList (3) is quite small so
          there will be little performance difference.
    */
    public ArrayList<HashMap<Card, Integer>> ydkImport(String fname) {
        int len = fname.length();
        //in case user has selected some file "a.py" and just using substring would throw
        if (len > 4 && fname.substring(len - 4).equals(".ydk")) {
            try {
                ArrayList<HashMap<Card, Integer>> decks = new ArrayList<>();
                /*
                  A ydk file is laid out in the form:
                    #created by ...
                    #main
                    id numbers for cards
                    .
                    .
                    .
                    #extra
                    id numbers for cards
                    .
                    .
                    .
                    !side
                    id numbers for cards
                    .
                    .
                    .
                  So, we want to be in the main deck (arraylist pos 0) at the second special char.
                  As such, we initialise deckCnt as -2 and increment it every time we see a '#' or '!'.
                 */
                int decksCnt = -2;
                BufferedReader ydkReader = new BufferedReader(new FileReader(fname));
                String nextLine;
                HashMap<Card, Integer> deck = new HashMap<>();

                /*
                  We stop when we reach a line that is null.
                  When we reach a new section we do an api call for that section (if it is non-empty).
                  This means we need to repeat a little bit of code after the while loop otherwise the side deck won't get api req'd
                 */
                while ((nextLine = ydkReader.readLine()) != null) {
                    char specialChar = nextLine.charAt(0);
                    if (specialChar == '#' || specialChar == '!') {
                        decksCnt++;
                        if (!deck.isEmpty()) {
                            decks.add(createCards(deck));
                            deck = new HashMap<>();
                        }
                    } else {
                        /*
                          Ideally we would be able to directly access the HashMap with the hashcode of just nextLine.
                          This would stop the new card object taking up space. However, with the java implementation of HashMap this is not possible.
                          A possible extension!
                         */

                        Card tempCard = new Card(Integer.parseInt(nextLine));
                        if (deck.containsKey(tempCard)) {
                            int curValue = deck.get(tempCard);
                            deck.replace(tempCard, curValue, curValue + 1);
                        } else {
                            deck.put(new Card(Integer.parseInt(nextLine)), 1);
                        }
                    }
                }
                decks.add(createCards(deck));
                return decks;
            } catch (FileNotFoundException f) {
                error("File not found", f);
            } catch (Exception e) {
                error("Unknown error occurred when opening your file", e);
            }
        } else {
            //error pop-up here
            System.out.println("Incorrect file type");
        }
        return null;
    }

    /*
      pre:  A HashMap of Cards and their quantity
      post: The same HashMap of Cards, but with the appropriate information from the api
     */
    public HashMap<Card, Integer> createCards(HashMap<Card, Integer> deck) {
        try {
            String apiRequest = "";
            Set<Card> cardSet = deck.keySet();
            for (Card card : cardSet) {
                apiRequest = apiRequest + card.getId() + ",";
            }
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://db.ygoprodeck.com/api/v7/cardinfo.php?id=" + apiRequest.substring(0, apiRequest.length() - 1)))
                    .build();
            JSONArray cardInfos = new JSONObject(client.send(request, HttpResponse.BodyHandlers.ofString()).body()).getJSONArray("data");
            for (int i = 0; i < cardInfos.length(); i++) {
                JSONObject jsonObject = cardInfos.getJSONObject(i);
                Card updatedKey = new Card(jsonObject.getInt("id"));
                updatedKey.updateCard(jsonObject);
                /*
                  Strange sequence here. As updatedKey == currentKey the key won't update with just .put.
                  As a result I need to do some strange switching thing.
                 */
                int tempQuant = deck.get(updatedKey);
                deck.remove(updatedKey);
                deck.put(updatedKey, tempQuant);
            }
            cardSet = deck.keySet();
            for (Card card : cardSet) {
                System.out.println(card.getName() + ", " + deck.get(card));
            }
            return deck;
        } catch (Exception e) {
            error("Unknown error occurred when fetching cards", e);
        }
        return null;
    }

    /*
      pre:  Error message to be displayed and Exception variable thrown from error
      post: Prints the error message and the stack trace of the exception variable
     */
    public void error(String errorMessage, Exception e) {
        //Some error pop-up
        System.out.println(errorMessage);
        e.printStackTrace();
    }

    public static void main(String[] args) {
        launch();
    }
}