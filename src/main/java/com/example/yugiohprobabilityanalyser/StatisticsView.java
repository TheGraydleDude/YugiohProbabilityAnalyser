package com.example.yugiohprobabilityanalyser;

import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class StatisticsView {

    private Deck model;

    private final HashMap<String, HashMap<Card, Integer>> listOfLists = new HashMap<>();

    @FXML
    private ComboBox<Card> cardSelector;

    @FXML
    private ComboBox<String> listSelector;

    @FXML
    private ScrollPane mainDeckPane;

    @FXML
    private ScrollPane sideDeckPane;

    @FXML
    private VBox currentList;

    @FXML
    private ComboBox<String> listSelectorInCalculator;

    @FXML
    private VBox desiredCards;

    @FXML
    private VBox undesiredCards;

    @FXML
    private ComboBox<String> turnSelector;


    /*
        pre:  scene initialised
        post: model set to the deck passed, scrollpanes filled, combobox filled
     */
    public void initData(Deck deck, GridPane mainDeck, GridPane sideDeck) {
        this.model = deck;
        this.mainDeckPane.setContent(mainDeck);
        if (sideDeck != null) {
            this.sideDeckPane.setContent(sideDeck);
        }
        fillCardSelector();
        turnSelector.getItems().addAll("Going First", "Going Second");
        turnSelector.setValue("Going First");
    }

    /*
        pre:  button is pressed
        post: we return to the previous scene
     */
    @FXML
    public void onBackButtonPress(Event event) {
        try {
            Node node = (Node) event.getSource();
            Stage stage = (Stage) node.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("deck-view.fxml"));
            stage.setScene(new Scene(loader.load()));
            //Have to do it this order (must load first)
            DeckView deckController = loader.getController();
            deckController.initData(model);
            stage.show();

        } catch (NullPointerException n) {
            System.out.println("File not chosen");
        } catch (Exception e) {
            error("Unknown error occurred", e);
        }
    }

    /*
        pre:  help button pressed
        post: help pop-up displayed
     */
    @FXML
    public void onHelpButtonPress() {
        Alert help = new Alert(Alert.AlertType.INFORMATION);

        help.setTitle("Help");
        help.setHeaderText("Help");
        help.setContentText("To use the calculator, select the card you want and its desired quantity." +
                "Then, add it to a specific 'list' of similar cards (e.g. 'handtraps'). You can remove cards from a list by pressing the remove button."
                + "Once you have defined all of your lists, use the equation generator to create your specific conditions"
                + " (e.g. ('handtraps' AND 2 x 'engine') AND (NOT 'garnets')). Determine whether you are going first or second and then press go!");
        try {
            ImageView cardView = new ImageView(SwingFXUtils.toFXImage(ImageIO.read(new File("src\\images\\CardBack.png")), null));
            cardView.setFitWidth(50);
            cardView.setPreserveRatio(true);
            help.setGraphic(cardView);
        } catch (IOException i) {
            error("IO error", i);
        } catch (Exception e) {
            error("Unknown error occurred", e);
        }

        help.show();
    }

    /*
        pre:  button to add card pressed
        post: the card, and number of, selected is added to the list selected and displayed in the "current list" tab
     */
    @FXML
    public void onAddButtonPress() {
        Card cardSelected = cardSelector.getValue();
        if (cardSelected != null) {
            TextInputDialog howManyToAdd = new TextInputDialog();

            howManyToAdd.setTitle("Adding Card");
            howManyToAdd.setHeaderText(cardSelected.getName());
            ImageView cardView = new ImageView(cardSelected.getCardImg());
            cardView.setFitWidth(50);
            cardView.setPreserveRatio(true);
            howManyToAdd.setGraphic(cardView);
            howManyToAdd.setContentText("How many " + cardSelected.getName() + "?");

            Optional<String> numToAdd = howManyToAdd.showAndWait();
            /*
                The first condition is not true when cancel is pressed, which is why no pop up asking to enter number.
                All the if statements are just checking valid input
             */
            if (numToAdd.isPresent()) {
                if (listSelector.getValue() != null) {
                    if (userInputIsInteger(numToAdd.get())) {
                        if (Integer.parseInt(numToAdd.get()) > 0) {
                            //So we don't exceed the number of cards we are playing
                            int numInMain = 0;
                            int numInSide = 0;
                            int numInListAlready = 0;
                            if (model.getMainDeck().containsKey(cardSelected)) {
                                numInMain = model.getMainDeck().get(cardSelected);
                            }
                            if (model.getSideDeck() != null) {
                                if (model.getSideDeck().containsKey(cardSelected)) {
                                    numInSide = model.getSideDeck().get(cardSelected);
                                }
                            }
                            if (listOfLists.get(listSelector.getValue()).containsKey(cardSelected)) {
                                numInListAlready = listOfLists.get(listSelector.getValue()).get(cardSelected);
                            }
                            if (Integer.parseInt(numToAdd.get()) + numInListAlready <= numInMain + numInSide) {
                                listOfLists.get(listSelector.getValue()).put(cardSelected, Integer.parseInt(numToAdd.get()));
                                for (int i = 0; i < Integer.parseInt(numToAdd.get()); i++) {
                                    Label toAddList = new Label(cardSelected.getName() + "\n");
                                    toAddList.setWrapText(true);
                                    currentList.getChildren().add(toAddList);
                                }
                            } else {
                                userBlankEntry("You do not have this many " + cardSelected.getName() + " in your deck");
                            }
                        }
                    } else {
                        userBlankEntry("Please make sure you enter just a number");
                    }
                } else {
                    userBlankEntry("Please choose a list");
                }
            }
        } else {
            userBlankEntry("Please choose a card first!");
        }
    }

    /*
        pre:  remove button pressed
        post: the selected card removed from the list and labels
     */
    @FXML
    public void onRemoveButtonPress() {
        if (cardSelector.getValue() != null) {
            Alert cardRemoval = new Alert(Alert.AlertType.CONFIRMATION);
            cardRemoval.setTitle("Removing a Card");
            cardRemoval.setHeaderText(cardSelector.getValue().getName());
            cardRemoval.setContentText("Remove all instances of " + cardSelector.getValue().getName() + "?");

            Optional<ButtonType> buttonType = cardRemoval.showAndWait();
            if (buttonType.get() == ButtonType.OK) {
                if (listSelector.getValue() != null) {
                    listOfLists.get(listSelector.getValue()).remove(cardSelector.getValue());
                    //You have to do it this way to avoid messing up the foreach loop
                    List<Node> forRemoval = new ArrayList<>();
                    for (Node label : currentList.getChildren()) {
                        if (((Label) label).getText().equals(cardSelector.getValue().getName() + "\n")) {
                            forRemoval.add(label);
                        }
                    }
                    currentList.getChildren().removeAll(forRemoval);
                } else {
                    userBlankEntry("Please select a list");
                }
            }
        } else {
            userBlankEntry("Please select a card");
        }
    }

    /*
        pre:  list chosen changes - either by clicking or by removing a list
        post: currentList updated with the new list
     */
    @FXML
    public void onListSelectorPress() {
        currentList.getChildren().clear();
        if (listSelector.getValue() != null) {
            for (Card card : listOfLists.get(listSelector.getValue()).keySet()) {
                for (int i = 0; i < listOfLists.get(listSelector.getValue()).get(card); i++) {
                    Label toAddList = new Label(card.getName() + "\n");
                    toAddList.setWrapText(true);
                    currentList.getChildren().add(toAddList);
                }
            }
        }
    }

    /*
        pre:  new list button pressed
        post: a pop-up is displayed asking for the name of the new list, which is then created
     */
    @FXML
    public void onNewListButtonPress() {
        try {
            TextInputDialog listNameReq = new TextInputDialog();

            listNameReq.setTitle("Creating new list");
            listNameReq.setHeaderText("New List");
            ImageView cardView = new ImageView(SwingFXUtils.toFXImage(ImageIO.read(new File("src\\images\\CardBack.png")), null));
            cardView.setFitWidth(50);
            cardView.setPreserveRatio(true);
            listNameReq.setGraphic(cardView);
            listNameReq.setContentText("Enter the name of the list you want to create");

            Optional<String> listName = listNameReq.showAndWait();
            if (listName.isPresent()) {
                if (listName.get().length() <= 8) {
                    if (!listName.get().contains("\"")) {
                        if (listOfLists.get(listName.get()) == null) {
                            listOfLists.put(listName.get(), new HashMap<>());
                            addToListComboBoxes(listName.get());
                            listSelector.setValue(listName.get());
                        } else {
                            userBlankEntry("That list already exists!");
                        }
                    } else {
                        userBlankEntry("The character \" is not allowed");
                    }
                } else {
                    userBlankEntry("Please make your list name 8 characters maximum");
                }
            }
        } catch (IOException i) {
            error("IO error", i);
        } catch (Exception e) {
            error("Unknown error occurred", e);
        }
    }

    /*
        pre:  remove list button pressed
        post: a pop-up confirming the deletion is shown then, if selected, the list is deleted
     */
    @FXML
    private void onRemoveListButtonPress() {
        if (listSelector.getValue() != null) {
            Alert removalConfirmation = new Alert(Alert.AlertType.CONFIRMATION);
            removalConfirmation.setTitle("Removing a List");
            removalConfirmation.setHeaderText(listSelector.getValue());
            removalConfirmation.setContentText("Do you want to delete the list: " + listSelector.getValue() + "?");
            try {
                ImageView cardView = new ImageView(SwingFXUtils.toFXImage(ImageIO.read(new File("src\\images\\CardBack.png")), null));
                cardView.setFitWidth(50);
                cardView.setPreserveRatio(true);
                removalConfirmation.setGraphic(cardView);
            } catch (IOException i) {
                error("IO error", i);
            } catch (Exception e) {
                error("Unknown error occurred", e);
            }

            Optional<ButtonType> buttonType = removalConfirmation.showAndWait();
            if (buttonType.get() == ButtonType.OK) {
                listOfLists.remove(listSelector.getValue());
                removeFromBothComboBoxes(listSelector.getValue());
                desiredCards.getChildren().clear();
                undesiredCards.getChildren().clear();
                //To update currentList
                onListSelectorPress();
            }
        } else {
            userBlankEntry("Please select a list");
        }
    }

    /*
        this has the same pre and post conditions as undesiredAndDesiredCards
     */
    @FXML
    public void onDesiredButtonPress() {
        if (listSelectorInCalculator.getValue() != null) {
            if (listOfLists.get(listSelectorInCalculator.getValue()).size() > 0) {
                TextInputDialog howManyToAdd = new TextInputDialog();

                howManyToAdd.setTitle("Adding List");
                howManyToAdd.setHeaderText("Desired List");
                Set<Card> listSet = listOfLists.get(listSelectorInCalculator.getValue()).keySet();
                ImageView cardView = new ImageView(listSet.iterator().next().getCardImg());
                cardView.setFitWidth(50);
                cardView.setPreserveRatio(true);
                howManyToAdd.setGraphic(cardView);
                howManyToAdd.setContentText("How many copies of cards in \"" + listSelectorInCalculator.getValue() + "\"do you want to see in your opening hand?");

                Optional<String> numToAdd = howManyToAdd.showAndWait();

                /*
                    The first condition is not true when cancel is pressed, which is why no pop up asking to enter number.
                    All the if statements are just checking valid input
                */
                if (numToAdd.isPresent()) {
                    if (userInputIsInteger(numToAdd.get())) {
                        if (Integer.parseInt(numToAdd.get()) <= listOfLists.get(listSelectorInCalculator.getValue()).values().stream().reduce(0, Integer::sum)) {
                            Label label = new Label(numToAdd.get() + "x cards from \"" + listSelectorInCalculator.getValue() + "\"");
                            label.setWrapText(true);
                            desiredCards.getChildren().add(label);
                        } else {
                            userBlankEntry("Please enter a valid number of cards");
                        }
                    } else {
                        userBlankEntry("Please enter a number");
                    }
                }
            } else {
                userBlankEntry("Please choose a non-empty list");
            }
        }
    }

    /*
        this has the same pre and post conditions as undesiredAndDesiredCards
     */
    @FXML
    public void onUndesiredButtonPress() {
        undesiredCards.getChildren().add(new Label(listOfLists.get(listSelector.getValue()).values().stream().reduce(0, Integer::sum) + "x cards from \"" + listSelectorInCalculator.getValue() + "\""));
    }

    /*
        pre:  clear button pressed
        post: desiredCards and undesiredCards empty
     */
    @FXML
    public void onClearButtonPress() {
        desiredCards.getChildren().clear();
        undesiredCards.getChildren().clear();
    }

    /*
        pre:  calculate button pressed
        post:
     */
    @FXML
    public void onCalculateButtonPress() {
        HashMap<HashMap<Card, Integer>, Integer> desiredHashMap = getDesiredUndesiredData(desiredCards.getChildren(), true);
        HashMap<HashMap<Card, Integer>, Integer> undesiredHashMap = getDesiredUndesiredData(undesiredCards.getChildren(), false);

        if (desiredHashMap.size() > 0 || undesiredHashMap.size() > 0) {
            int numOfCardsInHand = 5;
            if (turnSelector.getValue().equals("Going Second")) {
                numOfCardsInHand = 6;
            }

            int numOfDesired = desiredHashMap.values().stream().reduce(0, Integer::sum);
            //Checking that number of cards in hand and number of cards in deck are valid
            if (numOfDesired <= numOfCardsInHand && model.getMainDeck().values().stream().reduce(0, Integer::sum) - undesiredHashMap.values().stream().reduce(0, Integer::sum) >= numOfCardsInHand) {

                //Checking that there are no duplicate cards or lists
                if (!Collections.disjoint(desiredHashMap.keySet(), undesiredHashMap.keySet())) {
                    userBlankEntry("You cannot have the same list in desired and undesired");
                    return;
                }

                Set<HashMap<Card, Integer>> hashSet = new HashSet<>();
                hashSet.addAll(desiredHashMap.keySet());
                hashSet.addAll(undesiredHashMap.keySet());
                Set<Card> cardSet = new HashSet<>();
                for (HashMap<Card, Integer> list : hashSet) {
                    if (!Collections.disjoint(cardSet, list.keySet())) {
                        userBlankEntry("You cannot have the same card in 2 different lists");
                        return;
                    }
                    cardSet.addAll(list.keySet());
                }

                double probabilityOfDesired = 0;
                double probabilityOfUndesiredGivenDesired = 0;
                double totalProbability = 0;
                if(desiredHashMap.size() > 0){
                    probabilityOfDesired = calculateProb(desiredHashMap,numOfCardsInHand, numOfCardsInHand);
                    totalProbability = probabilityOfDesired;
                } else if (undesiredHashMap.size() > 0) {
                    probabilityOfUndesiredGivenDesired = 1 - calculateProb(undesiredHashMap, numOfCardsInHand, numOfCardsInHand);
                    totalProbability = probabilityOfUndesiredGivenDesired;
                }
                if(desiredHashMap.size() > 0 && undesiredHashMap.size() > 0){
                    /*
                    P(A n B) = P(A | B) * P(B)
                 */
                    totalProbability = probabilityOfUndesiredGivenDesired * probabilityOfDesired;
                }
                if (totalProbability > 0) {
                    Alert probabilityPopUp = new Alert(Alert.AlertType.INFORMATION);

                    probabilityPopUp.setTitle("Probability");
                    probabilityPopUp.setHeaderText("Result");
                    probabilityPopUp.setContentText("The probability of opening a hand like the one you specified is: " + totalProbability);

                    if (desiredHashMap.size() > 0) {
                        Set<HashMap<Card, Integer>> hashMapSet = desiredHashMap.keySet();
                        Set<Card> listSet = hashMapSet.iterator().next().keySet();
                        ImageView cardView = new ImageView(listSet.iterator().next().getCardImg());
                        cardView.setFitWidth(50);
                        cardView.setPreserveRatio(true);
                        probabilityPopUp.setGraphic(cardView);
                    } else {
                        try {
                            ImageView cardView = new ImageView(SwingFXUtils.toFXImage(ImageIO.read(new File("src\\images\\CardBack.png")), null));
                            cardView.setFitWidth(50);
                            cardView.setPreserveRatio(true);
                            probabilityPopUp.setGraphic(cardView);
                        } catch (IOException i) {
                            error("IO error", i);
                        } catch (Exception e) {
                            error("Unknown error occurred", e);
                        }
                    }

                    probabilityPopUp.show();
                }

            }
        }
    }

    /*
        pre:  calculate button pressed
        post: probability calculated
     */
    private double calculateProb(HashMap<HashMap<Card, Integer>, Integer> listsInA, int handSize, int adjustedHandSize) {
        /*
            P(A | B) = P(A n B) / P(B)
            P(A n B) = P(A | B) * P(B)

            Say we have:
            1x list A
            2x list B
            1x list C

            so we do P((A n B) n C) = P((A n B) | C) * P(C)
            = P((A n B) | C) * hypergeometric dist. for C
            P((A n B) | C) = P(A n B) where hand size = hand size - 1 and deck size = deck size - 1
            P(A n B) where (hs = hs-1, ds = ds-1) = P(A | B) * P(B)
            = P(A | B) * P(B) where (hs = hs-1, ds = ds-1)
            P(A | B) where (hs = hs-1, ds = ds-1)
            = P(A) where (hs = hs-3, ds = ds-3)

            So can we do some recursive thing?
            P(A n B) calculated by:
            calculateProb(All lists in A, B, handsize):
                if(B is null):
                    return hypergeometric(A)
                else:
                    return (calculateProb(All lists in A / new B, new B, handsize - B.size) * hypergeometric(B))
         */
        HashMap<Card, Integer> B = listsInA.keySet().iterator().next();
        int numOfB = listsInA.get(B);
        listsInA.remove(B);
        if (listsInA.isEmpty()) {
            /*
                Looks complicated but not:
                popSize is the number of cards in the main deck - the amount adjusted by the handSize changing
                sampleSize is the adjustedHandSize
                successesInPop is the size of the list
                successesInHand is the expected number of cards
             */
            return hypergeometric(model.getMainDeck().values().stream().reduce(0, Integer::sum) - (handSize - adjustedHandSize), adjustedHandSize, B.values().stream().reduce(0, Integer::sum), numOfB);
        } else {
            return (calculateProb(listsInA, handSize, handSize - numOfB) * hypergeometric(model.getMainDeck().values().stream().reduce(0, Integer::sum) - (handSize - adjustedHandSize), adjustedHandSize, B.values().stream().reduce(0, Integer::sum), numOfB));
        }
    }

    /*
        Implementation of the hypergeometric formula
     */
    private double hypergeometric(int popSize, int sampleSize, int successesInPop, int successesInSample) {
        return (double) (binomial(successesInPop, successesInSample) * binomial(popSize - successesInPop, sampleSize - successesInSample)) / (double) (binomial(popSize, sampleSize));
    }

    /*
        Implementation of the binomial coefficient function
     */
    private static long binomial(int n, int k) {
        if (k > n - k)
            k = n - k;

        long b = 1;
        for (int i = 1, m = n; i <= k; i++, m--)
            b = b * m / i;
        return b;
    }

    /*
        pre:  calculate button pressed
        post: data from the desired/undesired labels are fetched
     */
    private HashMap<HashMap<Card, Integer>, Integer> getDesiredUndesiredData(ObservableList<Node> labels, boolean desired) {
        HashMap<HashMap<Card, Integer>, Integer> toReturn = new HashMap<>();

        if (labels != null) {
            for (Node label : labels) {
                String labelText = ((Label) label).getText();
                int numOfCards = 1;
                if(desired){
                    numOfCards = Integer.parseInt(labelText.split("x")[0]);
                }
                String listName = labelText.split("\"")[1];
                toReturn.put(listOfLists.get(listName), numOfCards);
            }
        }

        return toReturn;
    }

    /*
        pre:  scene initialised
        post: the card selector filled with the names of the cards in main and side deck
     */
    private void fillCardSelector() {
        Set<Card> cardSet = new HashSet<>();
        cardSet.addAll(model.getMainDeck().keySet());
        if (model.getSideDeck() != null) {
            cardSet.addAll(model.getSideDeck().keySet());
        }
        for (Card card : cardSet) {
            cardSelector.getItems().add(card);
        }
    }

    /*
        function to add a string to both comboboxes, and thus keep them synchronised
     */
    private void addToListComboBoxes(String newList) {
        listSelector.getItems().add(newList);
        listSelectorInCalculator.getItems().add(newList);
    }

    /*
        The same function as above, but for removing
     */
    private void removeFromBothComboBoxes(String newList) {
        listSelector.getItems().remove(newList);
        listSelectorInCalculator.getItems().remove(newList);
    }

    /*
       pre:  input entered by  the user
       post: returns if that input is an integer
    */
    private boolean userInputIsInteger(String input) {
        Scanner sc = new Scanner(input.trim());
        if (sc.hasNextInt()) {
            return true;
        }
        return false;
    }

    /*
        pre:  user tries to add a blank card, or add to a blank list etc.
        post: error message pops up, asking the user to select something this time
     */
    private void userBlankEntry(String errorMessage) {
        Alert chooseACard = new Alert(Alert.AlertType.ERROR, errorMessage);
        chooseACard.show();
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
