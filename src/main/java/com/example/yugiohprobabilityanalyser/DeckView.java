package com.example.yugiohprobabilityanalyser;

import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

public class DeckView {

    private Deck model;

    @FXML
    private GridPane mainDeckPane;

    @FXML
    private GridPane sideDeckPane;

    @FXML
    private GridPane extraDeckPane;

    /*
        pre:  button is pressed
        post: we return to the previous scene
     */
    @FXML
    protected void onBackButtonPress(Event event) {
        try {
            Node node = (Node) event.getSource();
            Stage stage = (Stage) node.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("hello-view.fxml"));
            stage.setScene(new Scene(loader.load()));
            stage.show();
        } catch (IOException i) {
            error("IO error", i);
        } catch (Exception e) {
            error("Unknown error occurred", e);
        }
    }

    /*
        pre:  button pressed
        post: change to the statistics scene, and model passed
     */
    @FXML
    protected void onStatisticsButtonPress(Event event) {
        try {
            Node node = (Node) event.getSource();
            Stage stage = (Stage) node.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("statistics-view.fxml"));
            stage.setScene(new Scene(loader.load()));
            //Have to do it this order (must load first)
            StatisticsView statisticsController = loader.getController();

            GridPane sideDeckNull = null;
            if(sideDeckPane != null){
                sideDeckNull = sideDeckPane;
            }
            statisticsController.initData(model, mainDeckPane, sideDeckNull);
            stage.show();

        } catch (NullPointerException n) {
            System.out.println("File not chosen");
        } catch (Exception e) {
            error("Unknown error occurred", e);
        }
    }

    /*
        pre:  scene change
        post: set the model to the model passed to the controller.
              appropriate buttons shown from data from the model
     */
    public void initData(Deck deck) {
        model = deck;
        setUpLayout(deck.getMainDeck(), mainDeckPane);
        if(deck.getSideDeck() != null){
            setUpLayout(deck.getSideDeck(), sideDeckPane);
        }
        if(deck.getExtraDeck() != null){
            setUpLayout(deck.getExtraDeck(), extraDeckPane);
        }
    }

    /*
        pre:  scene initialised
        post: relevant buttons and labels created
     */
    private void setUpLayout(HashMap<Card, Integer> deckHashMap, GridPane pane) {
        Set<Card> mainDeck = deckHashMap.keySet();
        //j initialised to -1 as we immediately increment it to 0
        int i = 0;
        int j = -1;
        for (Card card : mainDeck) {
            //if a card is in the deck multiple times, we must make that many buttons for it
            for (int k = 0; k < deckHashMap.get(card); k++) {
                j++;
                if (j > 2) {
                    j = 0;
                    i++;
                }
                VBox cardLayout = createCardLayout(card);
                pane.add(cardLayout, j, i);
            }
        }
    }

    /*
        pre:  scene initialised
        post: the individual button/label combination is created for x card
     */
    private VBox createCardLayout(Card card) {
        Button cardFrame = new Button();
        cardFrame.setOnAction(e -> displayCardInfo(card));
        try{
            Image cardArt = card.getCardImg();
            ImageView view = new ImageView(cardArt);
            view.setFitWidth(90);
            view.setPreserveRatio(true);
            cardFrame.setGraphic(view);
        } catch (Exception e){
            //If the file is missing, using error would create 40+ error pop-ups, so I would just rather print this and have empty buttons
            e.printStackTrace();
        }

        Label cardName = new Label(card.getName());
        cardName.setMaxWidth(90);
        cardName.setWrapText(true);
        return new VBox(cardFrame, cardName);
    }

    /*
        pre:  button pressed
        post: pop-up with card information shown
     */
    private void displayCardInfo(Card card) {
        Alert cardInfo = new Alert(Alert.AlertType.INFORMATION);

        try{
            Image cardArt = card.getCardImg();
            ImageView view = new ImageView(cardArt);
            view.setFitWidth(50);
            view.setPreserveRatio(true);
            cardInfo.setGraphic(view);
        } catch (Exception e){
            //If the file is missing, using error would create 40+ error pop-ups, so I would just rather print this and have empty buttons
            e.printStackTrace();
        }

        cardInfo.setTitle(card.getName());
        cardInfo.setHeaderText(card.getName());

        // Dealing with the different types of card
        boolean isMonster = (card.getType().charAt(card.getType().length() - 1) == 'r');
        boolean isLink = (card.getType().charAt(0) == 'L');
        boolean isPend = (card.getType().charAt(0) == 'P');
        cardInfo.setContentText("Card Type: " + card.getType() + "\n" + (isMonster ? "Monster Type" : "Type") + ": " + card.getRace() +
                "\n" + (isMonster ? "ATK: " + card.getAtk() + "\n" +
                (isLink ? "Link Rating: " : "DEF: " + card.getDef() + "\nLevel: ") + card.getLevel() + "\nAttribute: " +
                card.getAttribute() + "\n" + (isPend ? "Scale " + card.getScale() + "\n" : "") : "") + card.getDesc());

        cardInfo.show();
    }

    /*
      pre:  Error message to be displayed and Exception variable thrown from error
      post: Prints the error message and the stack trace of the exception variable
     */
    private void error(String errorMessage, Exception e) {
        new Alert(Alert.AlertType.ERROR, errorMessage).show();
        e.printStackTrace();
    }
}
