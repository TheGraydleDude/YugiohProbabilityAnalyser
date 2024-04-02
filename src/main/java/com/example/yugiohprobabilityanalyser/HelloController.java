package com.example.yugiohprobabilityanalyser;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class HelloController {

    private final Deck model = new Deck();

    /*
        pre:  button clicked
        post: file selector shown, and that file appropriated loaded (if possible).
              then, scene switched to the deck-view scene
     */
    @FXML
    protected void onFileChooseButtonClick(Event event) {
        try {
            Node node = (Node) event.getSource();
            Stage stage = (Stage) node.getScene().getWindow();
            //Creating the file selector
            FileChooser fileChooser = new FileChooser();
            model.fillDeck(fileChooser.showOpenDialog(stage).toString());

            /*
                if the deck has actually been loaded in (if the file is a valid ydk)
                a ydk can be without a side or extra deck and be valid, however it cannot be without a main deck
             */
            if (model.getMainDeck() != null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("deck-view.fxml"));
                stage.setScene(new Scene(loader.load()));
                //Have to do it this order (must load first)
                DeckView deckController = loader.getController();
                deckController.initData(model);
                stage.show();
            }
        } catch (NullPointerException n) {
            System.out.println("File not chosen");
        } catch (Exception e) {
            error("Unknown error occurred", e);
        }
    }

    /*
      pre:  Error message to be displayed and Exception variable thrown from error
      post: Prints the error message and the stack trace of the exception variable
     */
    public void error(String errorMessage, Exception e) {
        new Alert(Alert.AlertType.ERROR, errorMessage).show();
        e.printStackTrace();
    }
}