package com.example.yugiohprobabilityanalyser;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Set;

/*
    Composite class provided to sort cards into each of their respective decks
    Also contains the code to read in a deck from a .ydk file
 */

public class Deck {
    private HashMap<Card, Integer> mainDeck;
    private HashMap<Card, Integer> extraDeck;
    private HashMap<Card, Integer> sideDeck;

    /*
    pre:  File name of the ydk we are trying to access
    post: ArrayList of HashMap of Cards and Integers, with the Cards as the key, containing main, side and extra decks.
          We use a HashMap as we do not know the exact size of the deck, and we want to also store the quantity of each card.
          The hash code the Cards is just the hashCode of its id, so the cards should be evenly spaced.
          Even though the outer ArrayList will be constant, java does not allow you to have
          an array of HashMaps, and the size of the outer ArrayList (3) is quite small so
          there will be little performance difference.
    */
    public void fillDeck(String fname) {
        int len = fname.length();
        //in case user has selected some file "a.py" and just using substring would throw
        if (len > 4 && fname.substring(len - 4).equals(".ydk")) {
            try {
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
                int decksCnt = 0;
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
                            if (decksCnt == 3) {
                                mainDeck = createCards(deck);
                            } else {
                                extraDeck = createCards(deck);
                            }
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
                if (!deck.isEmpty()) {
                    sideDeck = createCards(deck);
                }
            } catch (FileNotFoundException f) {
                error("File not found", f);
            } catch (Exception e) {
                error("Unknown error occurred when opening your file", e);
            }
        } else {
            new Alert(Alert.AlertType.ERROR, "Incorrect file type").show();
        }
    }

    /*
      pre:  A HashMap of Cards and their quantity
      post: The same HashMap of Cards, but with the appropriate information from the api.
            Any new card images downloaded
     */
    private HashMap<Card, Integer> createCards(HashMap<Card, Integer> deck) {
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
            downloadCards(deck);
            return deck;
        } catch (Exception e) {
            error("Unknown error occurred when fetching cards", e);
        }
        return null;
    }

    /*
        pre:  scene initialised
        post: card images not already downloaded, downloaded from the api and stored in \images folder

        The yugioh api IP bans those who request its api for card images too often, and as such I have to host them locally.
        This will download an image the first time a user uses that card, which can cause latency when they have a full deck of new cards.
        However, I can think of no better solution, as downloading 10,000+ images just to use one deck list would be silly
     */
    private void downloadCards(HashMap<Card, Integer> deck) {
        try {
            for (Card card : deck.keySet()) {
                String cardNameNoQuotes = card.getName().replaceAll("\"", "");
                if (!new File("src\\images\\" + cardNameNoQuotes + ".jpg").exists()) {
                    HttpClient client = HttpClient.newHttpClient();
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create("https://images.ygoprodeck.com/images/cards_small/" + card.getId() + ".jpg"))
                            .build();
                    Image img = new Image(client.send(request, HttpResponse.BodyHandlers.ofInputStream()).body());
                    ImageIO.write(SwingFXUtils.fromFXImage(img, null), "jpg", new File("src\\images\\" + cardNameNoQuotes + ".jpg"));
                    card.setCardImg(img);
                } else {
                    card.setCardImg(SwingFXUtils.toFXImage(ImageIO.read(new File("src\\images\\" + cardNameNoQuotes + ".jpg")), null));
                }
            }
        } catch (IOException | InterruptedException i) {
            error("Error downloading images", i);
        } catch (Exception e) {
            error("Unknown error occurred", e);
        }
    }

    /*
      pre:  Error message to be displayed and Exception variable thrown from error
      post: Prints the error message and the stack trace of the exception variable
     */
    private void error(String errorMessage, Exception e) {
        new Alert(Alert.AlertType.ERROR, errorMessage).show();
        e.printStackTrace();
    }

    public HashMap<Card, Integer> getMainDeck() {
        return mainDeck;
    }

    public HashMap<Card, Integer> getExtraDeck() {
        return extraDeck;
    }

    public HashMap<Card, Integer> getSideDeck() {
        return sideDeck;
    }
}
