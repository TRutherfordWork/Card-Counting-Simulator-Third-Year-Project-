package Blackjack;

import javafx.animation.PathTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;

public class BlackjackTable_Controller {

    public Pane playerCardPane;
    public Spinner betSpinner;
    public TextField bankBox;
    public Pane betSettingsPane;
    public Rectangle deckContainer;
    public Rectangle dealerContainer;
    public Rectangle playerOneContainer;
    public Pane blackJackTablePane;
    public Line dealerPath;
    public Line playerOnePath;
    public Line dealerPath2;
    public Line playerOnePath2;
    public Line playerOnePath3;
    public Line dealerPath3;
    public Line playerOnePath4;
    public Line playerOnePath5;
    public Line playerOnePath6;
    public Line dealerPath4;
    public Line dealerPath5;
    public Line dealerPath6;
    public Line dealersCardPath;
    public Button doubleDownButton;
    public Button standButton;
    public Button hitButton;
    public Button dealButton;


    //Card Counting Info Panes//

    public Text CCInfoName;
    public Text CCInfoRC;
    public Text CCInfoTC;
    public Text CCInfoBS;
    public Text CCInfoRB;
    public CheckBox CCEnableButton;


    ObservableList<String> data = FXCollections.observableArrayList();
    public ComboBox playerSelectComboBox;

    //realtime info text fields
    public Text currentHandInfo;
    public Text handValueInfo;
    public Text basicStrategyInfo;
    public Text decksInShoeInfo;
    public Text cardsInShoeInfo;
    public Text shoePenetrationInfo;
    public Text cardsInDeckInfo;
    public Text deckPenetrationInfo;

    int originalShoeSize;
    int originalShoeCardNum;

    ArrayList<Player> players;
    int playersPlaying;
    Shoe shoe;
    int shoeSize;
    String cardBack;
    String bankRoll;
    Hand dealerHand;
    int minBet;
    int cardNum = 1;
    //used to double bet if double down is chosen during round
    boolean doubleDown = false;

    //used to track image view of cards during animation
    Card lastCardOut;

    //used to track dealer's flip card
    Card dealerCard;

    //used to track dealer's shown card (used for Basic Strategy Decisions)
    Card dealerShownCard;

    //constructing TreeMap to process card values from retrieved enum tokens
    private TreeMap<Card.Value, String> valueTreeMap = new TreeMap<>();

    public void initialise(ArrayList<Player> players, Shoe shoe, int minBet, String cardBack){
        //initialising variables
        this.players = players;
        this.playersPlaying = players.size();
        this.shoe = shoe;
        this.cardBack = cardBack;
        this.shoeSize = shoe.getShoeSize();
        this.originalShoeSize = this.shoeSize;
        this.originalShoeCardNum = this.shoeSize * 52;
        this.minBet = minBet;

        bankRoll = String.valueOf(players.get(0).getBankRoll());

        //initialising widgets
        bankBox.insertText(0, bankRoll);

        //disabling buttons
        hitButton.setDisable(true);
        doubleDownButton.setDisable(true);
        standButton.setDisable(true);

        SpinnerValueFactory<Integer> betValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(minBet, (int) players.get(0).getBankRoll());
        betValueFactory.setValue(minBet);

        betSpinner.setValueFactory(betValueFactory);

        //initialising shoe
        this.shoe.constructShoe();

        //constructing dealer's hand
        dealerHand = new Hand();

        //initialising valueSet hashmap with corresponding tokens, Ace defined as 10 but will later be handled if need be)
        valueTreeMap.put(Card.Value.TWO, "2");
        valueTreeMap.put(Card.Value.THREE, "3");
        valueTreeMap.put(Card.Value.FOUR, "4");
        valueTreeMap.put(Card.Value.FIVE, "5");
        valueTreeMap.put(Card.Value.SIX, "6");
        valueTreeMap.put(Card.Value.SEVEN, "7");
        valueTreeMap.put(Card.Value.EIGHT, "8");
        valueTreeMap.put(Card.Value.NINE, "9");
        valueTreeMap.put(Card.Value.TEN, "10");
        valueTreeMap.put(Card.Value.JACK, "10");
        valueTreeMap.put(Card.Value.KING, "10");
        valueTreeMap.put(Card.Value.QUEEN, "10");
        valueTreeMap.put(Card.Value.ACE, "11");


        for(int i = 1; i < playersPlaying+1; i++){
            data.add("player" + i);
        }

        playerSelectComboBox.setItems(data);

        updateInfo();

    }

    public void updateCCInfo(Player player){

        ArrayList<String> data = player.getCardCountingMethodStats();

        CCInfoName.setText(data.get(0));
        CCInfoRC.setText(data.get(1));
        CCInfoTC.setText(data.get(2));
        CCInfoBS.setText(data.get(5) + "/" + data.get(3)); //BSRatio + BetSpread denominator
        CCInfoRB.setText(data.get(4));

    }

    public void updatePlayerInfo(){

        try {
            String player = playerSelectComboBox.getValue().toString();

            int index = data.indexOf(player);

            Player selectedPlayer = players.get(index);

            Hand playerHand = selectedPlayer.getPlayerHand();

            currentHandInfo.setText(playerHand.toString());
            handValueInfo.setText(String.valueOf(playerHand.getValue()));
            basicStrategyInfo.setText(getBasicStrategyDecision(selectedPlayer));

            updateCCInfo(selectedPlayer);

            //checking if CC method is active
            CCEnableButtonAction(new ActionEvent());
        } catch(Exception e) {
            System.out.println(e.toString() + " - In Function: 'UpdatePlayerInfo()'");
        }

    }

    public void updateInfo(){

        double shoePenetration = 100 - (((double) shoe.getShoeSize()/this.originalShoeSize) * 100);

        int cardsInDeck = (shoe.getCardsNum() - ((shoe.getShoeSize()-1)*52));

        double deckPenetration = 100 - (((double) cardsInDeck/52) * 100);

        decksInShoeInfo.setText(shoe.getShoeSize() + "/" + this.originalShoeSize);
        cardsInShoeInfo.setText(shoe.getCardsNum() + "/" + this.originalShoeCardNum);
        shoePenetrationInfo.setText(shoePenetration + "%");
        cardsInDeckInfo.setText(cardsInDeck + "/52");
        deckPenetrationInfo.setText(deckPenetration + "%");

    }

    public void dealAnimate(String destinationContainer, int cardNum, boolean visible){
        Image cardImage;

        double containerCoordX = 0;
        double containerCoordY = 0;
        //setting current coords to match that of the shoe on screen
        double shoeCoordX = deckContainer.getX();
        double shoeCoordY = deckContainer.getY();

        //System.out.println("Last Card Out:" + shoe.getDecks().get(0).getLastCardOut());

        //building imageView for card
        //if card is visible, add image of that card. If not, then add card back
        if(visible) {

            //getting last card information
            lastCardOut = shoe.getDecks().get(0).getLastCardOut();
            String value = lastCardOut.getValue().toString();
            String suit = lastCardOut.getSuit().toString().toLowerCase(Locale.ROOT);

            //System.out.println(value);
            //System.out.println(suit);

            //building filename to find
            String cardFileName = suit.toString().toLowerCase(Locale.ROOT) + "_" + value;

            //System.out.println("..\\Resources\\Cards\\" + suit + "\\" + cardFileName + ".png");

            cardImage = new Image(getClass().getResourceAsStream("..\\Resources\\Cards\\" + suit + "\\" + cardFileName + ".png"));

        } else {

            //setting dealer's "flip" card
            dealerCard = lastCardOut;
            cardImage = new Image(getClass().getResourceAsStream("..\\Resources\\Cards\\cardBacks\\blue.png"));
        }
        ImageView imageView = new ImageView(cardImage);
        imageView.setUserData("movingCardBackImage");

        imageView.setFitHeight(150);
        imageView.setFitWidth(104);

        //adding imageView to UI
        blackJackTablePane.getChildren().add(imageView);

        Node imageViewNode = null;

        //finding where the image view has been places via ID
        for(Node node : blackJackTablePane.getChildren()){
            if ((imageView.getUserData()).equals(node.getUserData())){
                imageViewNode = node;
            }
        }

        //setting image x and y to equal shoe's coordinates
        imageViewNode.setLayoutX(shoeCoordX);
        imageViewNode.setLayoutY(shoeCoordY);


        PathTransition transition = new PathTransition();
        transition.setNode(imageViewNode);
        transition.setDuration(Duration.seconds(1));

        if(cardNum == 1) {
            switch (destinationContainer) {
                case "dealerContainer" -> transition.setPath(dealerPath);
                case "playerOneContainer" -> transition.setPath(playerOnePath);
            }
        }
        if(cardNum == 2) {
            switch (destinationContainer) {
                case "dealerContainer" -> transition.setPath(dealerPath2);
                case "playerOneContainer" -> transition.setPath(playerOnePath2);
            }
        }
        if(cardNum == 3) {
            switch (destinationContainer) {
                case "dealerContainer" -> transition.setPath(dealerPath3);
                case "playerOneContainer" -> transition.setPath(playerOnePath3);
            }
        }
        if(cardNum == 4) {
            switch (destinationContainer) {
                case "dealerContainer" -> transition.setPath(dealerPath4);
                case "playerOneContainer" -> transition.setPath(playerOnePath4);
            }
        }
        if(cardNum == 5) {
            switch (destinationContainer) {
                case "dealerContainer" -> transition.setPath(dealerPath5);
                case "playerOneContainer" -> transition.setPath(playerOnePath5);
            }
        }
        if(cardNum == 6) {
            switch (destinationContainer) {
                case "dealerContainer" -> transition.setPath(dealerPath6);
                case "playerOneContainer" -> transition.setPath(playerOnePath6);
            }
        }
        if(destinationContainer.equals("dealersCard")){
            transition.setPath(dealersCardPath);
        }

        transition.play();

        updateInfo();

    }


    private void flipDealerCard(){

        //setting last card out to reuse dealAnimateCode in order to show dealer card
        shoe.getDecks().get(0).setLastCardOut(dealerCard);
        //System.out.println(shoe.getDecks().get(0).getLastCardOut().toString());
        dealAnimate("dealersCard", 0, true);

    }


    public void dealToPlayers(Player player, int cardNum, boolean visible) throws InterruptedException {

        //System.out.println(player.getPlayerHand().toString() + "\n" + shoe.toString());
        if (shoe.isEmpty()) {
            shoe = new Shoe(shoeSize);
            shoe.constructShoe();
        }

        //getting first deck
        ArrayList<Deck> currentShoe = shoe.getDecks();
        //getting cards of deck
        ArrayList<Card> currentCards = currentShoe.get(0).getCards();

        //making sure shoe is not empty, if it is REFILL up to max shoe size and recurse dealToPlayer
        if(!shoe.getDecks().isEmpty()) {


            //making sure current deck is not empty, if it is remove deck from shoe and recurse dealToPlayer
            if (!currentCards.isEmpty()) {
                shoe.getDecks().get(0).draw(player.getPlayerHand());

                ///////////////CARD COUNTING CODE///////////////////////////////////////////////////////////////////////

                Card cardToParse = shoe.getDecks().get(0).getLastCardOut();
                cardCounter(player, cardToParse);

                ///////////////CARD COUNTING CODE///////////////////////////////////////////////////////////////////////

                //sending data off to animate
                dealAnimate("playerOneContainer", cardNum, visible);

            } else {
                shoe.removeDeck(0);
                //recursion
                dealToPlayers(player, cardNum, visible);
            }


        }else{
            shoe.setShoeSize(shoeSize);
            shoe.constructShoe();
            dealToPlayers(player, cardNum, visible);
        }

        //System.out.println(player.getPlayerHand().toString() + "\n" + shoe.toString());

    }

    public void dealToDealer(int cardNum, boolean visible){

        //System.out.println(dealerHand.toString() + "\n" + shoe.toString());
        if (shoe.isEmpty()) {
            shoe = new Shoe(shoeSize);
            shoe.constructShoe();
        }

        //getting first deck
        ArrayList<Deck> currentShoe = shoe.getDecks();
        //getting cards of deck
        ArrayList<Card> currentCards = currentShoe.get(0).getCards();

        //making sure shoe is not empty, if it is REFILL up to max shoe size and recurse dealToDealer
        if(!shoe.getDecks().isEmpty()) {


            //making sure current deck is not empty, if it is remove deck from shoe and recurse dealToDealer
            if (!currentCards.isEmpty()) {
                shoe.getDecks().get(0).draw(dealerHand);
                //setting last dealt to later set dealers card
                lastCardOut = shoe.getDecks().get(0).getLastCardOut();

                ///////////////CARD COUNTING CODE///////////////////////////////////////////////////////////////////////

                for(Player player : players) {
                    Card cardToParse = shoe.getDecks().get(0).getLastCardOut();
                    cardCounter(player, cardToParse);
                }

                ///////////////CARD COUNTING CODE///////////////////////////////////////////////////////////////////////

                //sending data off to animate
                dealAnimate("dealerContainer", cardNum, visible);

            } else {
                shoe.removeDeck(0);
                //recursion
                dealToDealer(cardNum, visible);
            }


        }else{
            shoe.setShoeSize(shoeSize);
            shoe.constructShoe();
            dealToDealer(cardNum, visible);
        }

        //System.out.println(dealerHand.toString() + "\n" + shoe.toString());


    }

    private void processDraw(Player player){
        double bet = Double.parseDouble(betSpinner.getValue().toString());

        //set player bankroll
        double playerBankRoll = player.getBankRoll();
        player.setBankRoll(playerBankRoll + bet);

        //update player1 onscreen bankBox to reflect changes
        bankBox.setText(String.valueOf(players.get(0).getBankRoll()));

    }

    private void processWin(Player player){
        double bet = Double.parseDouble(betSpinner.getValue().toString());

        if(this.doubleDown){
            bet = bet * 2;
        }

        //because payOut is 3 to 2
        double payOut = (bet) + (bet / 2);

        //setting player's winnings
        double playerBankRoll = player.getBankRoll();
        player.setBankRoll(playerBankRoll + payOut);

        //update player1 bankBox on screen to reflect their new balance
        bankBox.setText(String.valueOf(players.get(0).getBankRoll()));


    }


    private void processBet(int bet){

        Player player = players.get(0);
        double bank = player.getBankRoll();
        double newBank = bank - bet;

        //updating player bank
        player.setBankRoll(newBank);

        //updating onscreen bank
        bankBox.setText(String.valueOf(newBank));

    }


    public void dealButtonAction(ActionEvent actionEvent) throws InterruptedException, IOException {

        processBet((Integer) betSpinner.getValue());

        //at the end of logic clear all dealt cards
        for(Node node : blackJackTablePane.getChildren()){
            if ("movingCardBackImage".equals(node.getUserData())){
                node.setVisible(false);
            }
        }

        //resetting cardNum for new round
        cardNum = 1;
        //resetting dealers hand and player's hands for new round
        dealerHand = new Hand();

        for(Player player : players){
            player.clearPlayerHand();
        }

        //making sure player has enough money in the bank
        if(!(Double.parseDouble(bankBox.getText()) > minBet)){
            System.out.println("insufficient funds!");
            //taking user back to main menu if they have insufficient funds
            Main.changeMainScene(actionEvent, "./StartMenu.fxml");
        }


        //first round
        dealToDealer(cardNum, false);
        //assign dealer's flip card
        dealerCard = lastCardOut;
        //System.out.println("players = " + this.players.size());


        for(Player player : players) {
            dealToPlayers(player, cardNum, true);
            //System.out.println(player);
        }

        //incrementing cardNum (new round of cards)
        cardNum++;

        //second round
        dealToDealer(cardNum, true);

        dealerShownCard = lastCardOut;

        for(Player player : players) {
            dealToPlayers(player, cardNum, true);
            //added on 1/1/2022
            String decision = getBasicStrategyDecision(player);
            //System.out.println(decision);
        }

        //enabling game buttons
        hitButton.setDisable(false);
        doubleDownButton.setDisable(false);
        standButton.setDisable(false);

        //disabling deal button
        dealButton.setDisable(true);

        //disabling bank and bet box
        bankBox.setDisable(true);
        //betSpinner.setDisable(true); //handled in updatePlayerInfo()

        updatePlayerInfo();

        //setting the betSpinner to equal the recommended bet if the Card Counting Method has been enabled
        if(CCEnableButton.isSelected()) {
            betSpinner.setDisable(false);
            try {
                betSpinner.getValueFactory().setValue((Integer.parseInt(CCInfoRB.getText())));
            } catch(Exception e){
                System.out.println(e.toString() + " - In Function: 'CCEnableButtonAction()'");
            }
            betSpinner.setDisable(true);
        } else {
            betSpinner.setDisable(false);
        }
        betSpinner.setDisable(true);

    }

    private void dealerLogic(){

        int dealerTotal = dealerHand.getValue();

        if(dealerTotal < 17){
            //dealer has to hit on 16 and below
            //System.out.println(dealerTotal + "17 below");
            dealToDealer(cardNum, true);
            //recursion with incrementing dealer card num
            cardNum++;
            dealerLogic();
        }

        else {
            System.out.println(dealerTotal);
            compareHands(dealerTotal);
        }

    }

    public Integer getCardTotal(int playerIndex){
        int playerTotal = 0;
        Card.Value value;
        int intValue;
        ArrayList<Card.Value> cardList = new ArrayList<>();

        Player player = players.get(playerIndex);
        Hand playerHand = player.getPlayerHand();

        for(Card card : playerHand.cards){
            value = card.getValue();
            //appending card list in case of an ace
            cardList.add(0, value);
            //searching value in TreeMap
            intValue = Integer.parseInt(valueTreeMap.get(value));
            //incrementing dealer total
            playerTotal += intValue;
        }

        //dealer either busts or converts ace to 1 from 11
        if(playerTotal > 21 && cardList.contains(Card.Value.ACE)){
            System.out.println(playerTotal + "ace conversion");
            //ace conversion
            playerTotal = playerTotal - 10;
        }

        return playerTotal;

    }



    public String getBasicStrategyDecision(Player player){
        Card.Value dealerShownCardValue = dealerShownCard.getValue();
        int intValue = Integer.parseInt(valueTreeMap.get(dealerShownCardValue));

        return player.getBasicStrategyDecision(intValue);
    }



    private void compareHands(int dealerTotal){
        Integer playerOneTotal = getCardTotal(0);

        List<Integer> playerTotals = new ArrayList<Integer>();

        playerTotals.add(playerOneTotal);

        //used to show dealers hand to user
        flipDealerCard();

        for(Integer playerTotal : playerTotals){

            //getting the player who the total belongs to so the system can process winners
            int playerIndex = playerTotals.indexOf(playerTotal);
            Player currentPlayer = players.get(playerIndex);

            System.out.println("Player Total: " + playerTotal);
            System.out.println("Dealer Total: " + dealerTotal);
            //seeing who wins
            if(playerTotal > 21){

                alertBox("DEALER WINS", ("Player" + (playerTotals.indexOf(playerTotal)-1) + ": BUST! Dealer WINS!"), "unlucky, Player" + (playerTotals.indexOf(playerTotal)-1));

            }
            else if(playerTotal > dealerTotal){

                alertBox("PLAYER WINS", ("Player" + (playerTotals.indexOf(playerTotal)-1) + ": WINS against DEALER!"), "congratulations, Player" + (playerTotals.indexOf(playerTotal)-1));
                processWin(currentPlayer);

            }
            else if(dealerTotal > playerTotal && dealerTotal < 22){

                alertBox("DEALER WINS", ("Player" + (playerTotals.indexOf(playerTotal)-1) + ": Dealer WINS!"), "unlucky, Player" + (playerTotals.indexOf(playerTotal)-1));

            }
            else if(dealerTotal == playerTotal){

                alertBox("DRAW", ("Player" + (playerTotals.indexOf(playerTotal)-1) + ": Draws!"), "let's draw again, Player" + (playerTotals.indexOf(playerTotal)-1));
                processDraw(currentPlayer);

            }
            else{

                alertBox("PLAYER WINS", ("Player" + (playerTotals.indexOf(playerTotal)-1) + ": Dealer BUSTS, Player WINS"), "congratulations, Player" + (playerTotals.indexOf(playerTotal)-1));
                processWin(currentPlayer);

            }

        }

    }

    private void alertBox(String title, String headerText, String contentText){

        Alert variableDefinedAlert = new Alert(Alert.AlertType.INFORMATION);
        variableDefinedAlert.setTitle(title);
        variableDefinedAlert.setHeaderText(headerText);
        variableDefinedAlert.setContentText(contentText);

        variableDefinedAlert.showAndWait();

    }

    public void hitButtonAction(ActionEvent actionEvent) throws InterruptedException {
        //disabling doubleDownButton
        doubleDownButton.setDisable(true);

        //increment cardNum (which will update transition pathing etc)
        cardNum++;

        dealToPlayers(players.get(0), cardNum, true);
        //System.out.println(getBasicStrategyDecision(players.get(0)));

        updatePlayerInfo();
    }

    public void doubleDownButtonAction(ActionEvent actionEvent) throws InterruptedException {
        //processing bet again
        processBet((Integer) betSpinner.getValue());

        //setting doubleDown to true (used to double bet)
        this.doubleDown = true;

        //disabling doubleDown and hit buttons
        hitButton.setDisable(true);
        doubleDownButton.setDisable(true);

        //increment cardNum (which will update transition pathing etc)
        cardNum++;

        dealToPlayers(players.get(0), cardNum, true);
        standButtonAction(actionEvent);

        updatePlayerInfo();
    }

    public void standButtonAction(ActionEvent actionEvent) {
        //run through dealers actions and game logic
        
        //disabling game button except for deal
        hitButton.setDisable(true);
        doubleDownButton.setDisable(true);
        standButton.setDisable(true);
        
        //cardNum set to 3, as it is the dealers third card if it has to hit in the event of totaling less than 17
        cardNum = 3;

        //if no players have busted, do logic. if not go straight to compare hands
        int bustedPlayers = 0;
        int playerTotal;


        for(Player player : players){

            Hand hand = player.getPlayerHand();
            playerTotal = hand.getValue();

            if(playerTotal > 21){
                bustedPlayers++;
            }

        }

        if(bustedPlayers != playersPlaying){
            dealerLogic();
        }
        else{
            alertBox("DEALER WINS", ("All BUST! Dealer Wins"), "unlucky, everyone");
        }

        //enabling deal button
        dealButton.setDisable(false);

        //enabling bankBox and betSpinner
        bankBox.setDisable(false);
        //betSpinner.setDisable(false);
        updatePlayerInfo();

        //resetting doubleDown boolean
        this.doubleDown = false;

    }

    public void playerSelectComboBoxAction(ActionEvent actionEvent) {
        updatePlayerInfo();
    }

    ///////////////CARD COUNTING CODE///////////////////////////////////////////////////////////////////////
    public void cardCounter(Player player, Card card){

        //getting the card value of the card passed
        String cardValue = card.getValue().toString();
        String cardSuit = card.getSuit().toString();
        //running card counting method, passing the card value to be processed
        player.cardCountingMethodEvent(cardValue, cardSuit, shoe.getShoeSize());

    }

    public void CCEnableButtonAction(ActionEvent actionEvent) {
        System.out.println(minBet);
        if(CCEnableButton.isSelected()) {
            betSpinner.setDisable(false);
            try {
                betSpinner.getValueFactory().setValue((minBet));
            } catch(Exception e){
                System.out.println(e.toString() + " - In Function: 'CCEnableButtonAction()'");
            }
            betSpinner.setDisable(true);
        } else {
            betSpinner.setDisable(false);
        }
    }
}
