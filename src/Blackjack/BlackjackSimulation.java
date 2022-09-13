package Blackjack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

public class BlackjackSimulation {

    private TreeMap<Card.Value, String> cardValues = new TreeMap<>();

    //countFreqMap exists in order to collect average count frequencies from all games in simulation objects
    private HashMap<String, Integer> countFreqMap = new HashMap<>();

    //this exists in order to collect win loss data from games where the count is at a specific number
    private HashMap<String, List<Integer>> countWLDMap = new HashMap<>();


    private int shoeSize;
    private Player player;
    private double bankRoll;
    private int betSpread;
    private int minBet;
    private int numberOfRoundsToBePlayed;
    private int roundsPlayed = 0;

    private int recommendedBet;

    //variables used in round
    private Hand dealersHand;
    private Shoe shoe;
    private boolean doubleDown = false;

    //used in the case that recommendedBet cannot be afforded, but minBet can.
    private int usedBet;

    //keep track of if the player has ran out of money completely resulting in game over
    boolean gameOver = false;

    //Card Counting Information Variables
    private String CCInfoName;
    private String CCInfoRC;
    private String CCInfoTC;
    private String CCInfoBS;
    private String CCInfoRB;
    private int count;

    //deck penetration variables
    private int targetDeckPenetration;
    private double deckPenetration;
    private int cardsInDeck;

    //play-style variables
    private boolean wongingBool;


    BlackjackSimulation(Shoe shoe, TreeMap cardValues, Player player, int decksInShoe, int bankRoll, int betSpread, int minBet, int RPH, int HPG, int targetDeckPenetration, SimulationBankTracker bankTracker, boolean wongingBool){

            //initializing stat maps
            initializeStatMaps();

            this.cardValues = cardValues;
            this.shoe = new Shoe(decksInShoe);
            this.shoe.constructShoe();

            this.shoeSize = decksInShoe;
            this.player = player;
            this.bankRoll = bankRoll;
            this.betSpread = betSpread;
            this.minBet = minBet;
            this.recommendedBet = minBet;

            this.targetDeckPenetration = targetDeckPenetration;

            //working out the number of games to be played in this worker
            this.numberOfRoundsToBePlayed = RPH * HPG;

            this.wongingBool = wongingBool;

            int i;
            for(i=1; i < this.numberOfRoundsToBePlayed+1; i++){
                if(this.wongingBool == true){
                    //ensuring deck penetration
                    setDeckPenetration(targetDeckPenetration, player);
 
                    int count;
                    if(player.getCardCountingMethodType() == "Running Count"){
                        count = Integer.parseInt(CCInfoRC);
                    } else {
                        count = Integer.parseInt(CCInfoTC);
                    }
                    if(count >= 4.0){
                        if(!gameOver) {
                            this.updateCCInfo(player);
                            this.updatePlayerInfo();

                            calculateCount(player);

                            simulateRound();

                        } else {
                            System.out.println("GAME OVER");
                            System.out.println("Bank roll: " + player.getBankRoll());
                            System.out.println("minBet: " + minBet);
                            System.out.println("recommended bet: " + recommendedBet);
                            i = this.numberOfRoundsToBePlayed+1;
                            gameOver = true;
                            break;
                        }
                        //adding bankRoll information to bankTracker object
                        bankTracker.addRoundData(i, player.getBankRoll());
                    } else {
                        passRound();
                        i = i - 1;
                    }
                } else {

                    if(!gameOver) {

                        //ensuring deck penetration
                        setDeckPenetration(targetDeckPenetration, player);
                        this.updateCCInfo(player);
                        this.updatePlayerInfo();

                        calculateCount(player);

                        simulateRound();

                    } else {
                        System.out.println("GAME OVER");
                        System.out.println("Bank roll: " + player.getBankRoll());
                        System.out.println("minBet: " + minBet);
                        System.out.println("recommended bet: " + recommendedBet);
                        i = this.numberOfRoundsToBePlayed+1;
                        gameOver = true;
                        break;
                    }
                    //adding bankRoll information to bankTracker object
                    bankTracker.addRoundData(i, player.getBankRoll());

                }

            }
            //System.out.println("final bankroll : " + this.bankRoll);


    }

    private void passRound() {

        //checking if shoe is empty, if it is refill and reset CC method
        if (shoe.isEmpty()) {
            shoe = new Shoe(shoeSize);
            shoe.constructShoe();
            //resetting player's card counting method when shoe is empty
            player.resetCardCountingMethod();
        }

        ArrayList<Deck> currentShoe = this.shoe.getDecks();
        ArrayList<Card> currentCards = currentShoe.get(0).getCards();

        if(!currentCards.isEmpty()) {
            Card cardOut = shoe.getDecks().get(0).getCard(0);
            shoe.getDecks().get(0).removeCard(0);
            player.cardCountingMethodEvent(cardOut.getValue().toString(), cardOut.getSuit().toString(), shoe.shoeSize);
        }
        else{
            shoe.removeDeck(0);
            //recursion
            passRound();
        }


    }

    public void setDeckPenetration(int targetDeckPenetration, Player player){

        int cardsInDeck = (shoe.getCardsNum() - ((shoe.getShoeSize()-1)*52));
        //System.out.println(shoe.getCardsNum());
        //System.out.println(shoe.getShoeSize());
        double deckPenetration = 100 - (((double) cardsInDeck/52) * 100);
        //System.out.println(cardsInDeck);

        //setting card, which will equal the card to be removed so that the running count can be updated for player
        Card cardOut;

        //removing cards from shoe until targetDeckPenetration percentage is reached
        while((double) targetDeckPenetration > deckPenetration){
            if(deckPenetration > 80){
                System.exit(1);
            }
            //.println("target: " + targetDeckPenetration);
            //System.out.println("actual: " + deckPenetration);

            cardOut = shoe.getDecks().get(0).getCard(0);
            shoe.getDecks().get(0).removeCard(0);
            cardsInDeck = (shoe.getCardsNum() - ((this.shoeSize-1)*52));
            deckPenetration = 100 - (((double) cardsInDeck/52) * 100);

            //updating player running count
            //player.cardCountingMethodEvent(cardValue, cardSuit, shoe.getShoeSize());
            player.cardCountingMethodEvent(cardOut.getValue().toString(), cardOut.getSuit().toString(), shoe.shoeSize);

        }

        updatePlayerInfo();
        updateCCInfo(player);

    }

    private void simulateRound() {

        //first need to deal the cards out to dealer and player
        //the bet is first calculated via the player's card counting method
        //deciding which bet can be afforded, the recommended bet or the minBet
        //if the recommended bet cannot be afforded the bet will be set to the player's bankroll number only if
        //the number is greater than the minbet (equal to bankRoll / bettingUnits)

        roundsPlayed += 1;

        updatePlayerInfo();
        updateCCInfo(player);
        recommendedBet = (int) processCardCountingMethod();

        //System.out.println("current count --------------------->>>>> " + CCInfoRC);

        //System.out.println("RECOMMENDED BET -------------->>>> " + recommendedBet);

        if (bankRoll >= recommendedBet){
            usedBet = recommendedBet;
        }
        else if (bankRoll >= minBet){
            usedBet = (int) bankRoll;
        }
        else {
            gameOver = true;
        }

        if(!gameOver) {
            dealAction(usedBet);
            decideAction();
            //System.out.println("New BankRoll = " + bankRoll);
            cardCounter(player, dealersHand.getCard(1));
            //System.out.println("RunningCount =" +CCInfoRC);
            //System.out.println("TrueCount =" +CCInfoRC);
        }

    }

    private double processCardCountingMethod(){
        return Double.parseDouble(this.CCInfoRB);
    }

    private void decideAction(){

        /*
        if (decision == "HIT"){
            playerHit();
            decideAction();
        } else if (decision == "STAND"){
            playerStand();
        } else if (decision == "DOUBLE DOWN"){
            playerDoubleDown(recommendedBet);
        } else if (decision == "SPLIT"){
            playerHit();
            decideAction();
        }
        */

        //getting basic strategy dicision
        String decision = getBasicStrategyDecision(player);
        //System.out.println(decision);
        //.println("DECISION = " + decision);

        //System.out.println("---------------------------------------------------- decision: " + decision);


        //preventing splitting after splitting
        /*if(player.splitHand && decision.equals("SPLIT")){
            if(player.playerHand.getValue() < 17){
                decision = "HIT";
            } else {
                decision = "STAND";
            }
        }*/

        switch(decision){
            case "HIT" -> {
                playerHit();
                decideAction();
            }
            case "STAND" -> playerStand();
            case "SPLIT" -> {
                                 if(bankRoll > recommendedBet * 2) {
                                     playerSplit();
                                 } else{
                                     if(player.playerHand.getValue() < 17) {
                                         playerHit();
                                     } else {
                                         playerStand();
                                     }
                                 }
            }
        }


        //if Double Down is recommended there are numerous things that can happen:
            //1. Double down is accepted and recommended CC bet is used as it can be afforded
            //2. Double down is accepted but minBet is used as recommended bet cannot be afforded
            //3. Double down is not accepted as minBet cannot be afforded, hit instead.

        //System.out.println("BANKROLL:   " + bankRoll);
        //System.out.println("Recommended Bet:    " + recommendedBet);

        if(decision.equals("DOUBLE DOWN") && bankRoll > usedBet){
            playerDoubleDown(usedBet);
        } else if (decision.equals("DOUBLE DOWN")){
            //used if player cannot afford the usedBet, no double down occurs - instead just hit
            playerHit();
        }

    }

    private void playerStand() {

        updatePlayerInfo();

        //run through dealer's actions and game logic
        Hand hand = player.getPlayerHand();
        int playerTotal = hand.getValue();

        //System.out.println("player Total:    " + playerTotal);

        if(playerTotal > 21){

            //System.out.println("Player Hand: " + player.getPlayerHand().toString());
            //System.out.println("Dealer Hand: " + dealersHand.toString());
            //System.out.println("Player Total: " + playerTotal);
            //System.out.println("Dealer Total: " + dealersHand.getValue());


            processLoss();
        } else {
            dealerLogic();
        }

        //resetting double down
        this.doubleDown = false;

    }

    private void playerDoubleDown(int bet){
        processBet(bet);

        //setting the double down to true (used to double bet)
        this.doubleDown = true;

        dealToPlayer();
        playerStand();
    }

    private void playerHit() {
        dealToPlayer();
    }



    private void dealerLogic() {

        int dealerTotal = dealersHand.getValue();

        if(dealerTotal < 17){
            //dealer has to hit on 16 and below
            dealToDealer();
            dealerLogic();
        }
        else{
            compareHands(dealerTotal);
        }

    }

    private void compareHands(int dealerTotal) {

        Integer playerTotal = player.getPlayerHand().getValue();

        //System.out.println("Player Hand: " + player.getPlayerHand().toString());
        //System.out.println("Dealer Hand: " + dealersHand.toString());
        //System.out.println("Player Total: " + playerTotal);
        //System.out.println("Dealer Total: " + dealerTotal);

        if(dealerTotal > 21){
            //System.out.println("Dealer BUSTS, PLAYER WINS");
            processWin();
        }
        else if (playerTotal > dealerTotal){
            //System.out.println("PLAYER WINS");
            processWin();
        }
        else if (dealerTotal > playerTotal && dealerTotal < 22){
            //System.out.println("DEALER WINS");
            processLoss();
        }
        else if (dealerTotal == playerTotal){
            //System.out.println("DRAW");
            processDraw();
        }
        else {
            //System.out.println("Dealer BUSTS, PLAYER WINS");
            processWin();


        }

    }

    private void processDraw() {

        double bet = recommendedBet;

        //set player bankRoll
        double playerBankRoll = player.getBankRoll();
        player.setBankRoll(playerBankRoll + bet);

        updatePlayerInfo();

        /////////STATS CODE///////////
        incrementCountFreq();
        countWinLoss("DRAW");
        /////////STATS CODE///////////

    }

    private void processWin() {
        double bet = recommendedBet;

        if(this.doubleDown){
            bet = bet * 2;
        }

        //because payOut is 3 to 2
        double payOut = (bet) + (bet / 2);

        //setting player's winnings
        double playerBankRoll = player.getBankRoll();
        player.setBankRoll(playerBankRoll + payOut);

        updatePlayerInfo();

        /////////STATS CODE///////////
        incrementCountFreq();
        countWinLoss("WIN");
        /////////STATS CODE///////////

    }

    private void processLoss() {

        /////////STATS CODE///////////
        incrementCountFreq();
        countWinLoss("LOSS");
        /////////STATS CODE///////////

    }


    private void dealAction(int bet){

        processBet(bet);

        //resetting dealers hand and player's hand for new round
        dealersHand = new Hand();
        player.clearPlayerHand();

        //first round
        dealToDealer();

        dealToPlayer();


        //secondRound
        dealToDealer();

        dealToPlayer();

        updateCCInfo(player);
        updatePlayerInfo();

    }

    private void dealToPlayer() {

        if (shoe.isEmpty()) {
            shoe = new Shoe(shoeSize);
            shoe.constructShoe();
            //resetting player's card counting method when shoe is empty
            player.resetCardCountingMethod();
        }


        //getting first deck
        ArrayList<Deck> currentShoe = this.shoe.getDecks();
        //getting cards of the deck
        ArrayList<Card> currentCards = currentShoe.get(0).getCards();

        //if the shoe is empty REFILL up to the max shoe size and recurse dealToPlayer
        if(!shoe.isEmpty()){

            //making sure the current deck is not empty, if it is remove deck from shoe and recurse dealToPlayer
            if(!currentCards.isEmpty()) {
                shoe.getDecks().get(0).draw(player.getPlayerHand());

                ///////////////CARD COUNTING CODE///////////////////////////////////////////////////////////////////////
                Card cardToParse = shoe.getDecks().get(0).getLastCardOut();
                cardCounter(player, cardToParse);

                ///////////////CARD COUNTING CODE///////////////////////////////////////////////////////////////////////

            } else {
                shoe.removeDeck(0);
                //recursion
                dealToPlayer();
            }

        }


    }

    private void dealToDealer() {

        if (shoe.isEmpty()) {
            shoe = new Shoe(shoeSize);
            shoe.constructShoe();
            //resetting player's card counting method when shoe is empty
            player.resetCardCountingMethod();
        }

        //getting first deck
        ArrayList<Deck> currentShoe = this.shoe.getDecks();
        //System.out.println("shoe size: " + currentShoe.size());
        //getting cards of the deck
        ArrayList<Card> currentCards = currentShoe.get(0).getCards();
        //System.out.println("cards in shoe: " + currentCards.size());

        //if the shoe is empty REFILL up to the max shoe size and recurse dealToDealer
        if(!shoe.isEmpty()){

            //making sure the current deck is not empty, if it is remove deck from shoe and recurse dealToDealer
            if(!currentCards.isEmpty()) {
                shoe.getDecks().get(0).draw(dealersHand);

                ///////////////CARD COUNTING CODE///////////////////////////////////////////////////////////////////////
                Card cardToParse = shoe.getDecks().get(0).getLastCardOut();

                if(dealersHand.cards.size() < 2) { //flip card to be counted later
                    cardCounter(player, cardToParse);
                }

                ///////////////CARD COUNTING CODE///////////////////////////////////////////////////////////////////////

            } else {
                shoe.removeDeck(0);
                //recursion
                dealToDealer();
            }

        }

    }

    private void processBet(int bet) {

        double bank = this.player.getBankRoll();
        double newBank = bank - bet;

        //updating the player bank
        player.setBankRoll(newBank);
        this.bankRoll = newBank;

    }

    public String getBasicStrategyDecision(Player player){
        Card.Value dealerShownCardValue = dealersHand.cards.get(0).getValue();
        int intValue = Integer.parseInt(cardValues.get(dealerShownCardValue));

        String decision = player.getBasicStrategyDecision(intValue);
        //////////////CARD COUNTING CODE/////////////
        String deviationDecision = player.getCardCountingDeviationDecision(intValue);

        if(deviationDecision != null){
            decision = deviationDecision;
        }

        //////////////CARD COUNTING CODE/////////////

        return decision;

    }

    //////////////////SPLITTING HANDS!////////////////////

    public void playerSplit(){
        /*the purpose of this function is to "split" the player's hand into two when necessary
        HOW TO SPLIT:

        1. Player splits hand into two new hands -----> with player.splitHand

        2. Player then draws into both newly created hands (call hit method in this class)

        3. Game is then played with an array of hands (elements 0 and 1 representing splits)

        4. A player cannot split on a split hand as per rules of DAS but may 'D'ouble 'A'fter 'S'plit

         */

            //1.
        player.splitHand();

            //2 (need to loop through playerHands)
        for(Hand hand : player.splitHandsArrayList) {

            //setting the playerHand so that input hand can be processed normally
            player.playerHand = hand;

            dealToPlayer();

        }

        //System.out.println("SPLIT HAND! : " + player.splitHandsArrayList.get(0).toString() +
                            //"\n" + player.splitHandsArrayList.get(1).toString());

        simulateSplitRound();

    }


    public void simulateSplitRound(){

        updatePlayerInfo();
        updateCCInfo(player);

        for(Hand hand : player.splitHandsArrayList){

            //setting the playerHand so that input hand can be processed normally
            player.playerHand = hand;

            updateCCInfo(player);

            //System.out.println("current count SPLIT HAND --------------------->>>>> " + CCInfoRC);

            //System.out.println("RECOMMENDED BET SPLIT HAND -------------->>>> " + recommendedBet);

            if(!gameOver) {
                playerHit();
                decideAction();
                //System.out.println("New BankRoll = " + bankRoll);
            }

        }

        player.resetPlayerSplit();

    }






    //////////////////CARD COUNTING//////////////////////


    private void cardCounter(Player player, Card card) {

        //getting the card value of the card passed
        String cardValue = card.getValue().toString();
        String cardSuit = card.getSuit().toString();
        //running card counting method, passing the card value to be processed
        player.cardCountingMethodEvent(cardValue, cardSuit, shoe.getShoeSize());

        updateCCInfo(player);

        //count = Integer.parseInt(CCInfoTC);

    }

    private void updatePlayerInfo(){

        this.bankRoll = this.player.getBankRoll();
        updateCCInfo(this.player);

    }

    private void updateCCInfo(Player player){

        ArrayList<String> data = player.getCardCountingMethodStats();

        CCInfoName = (data.get(0));
        CCInfoRC = (data.get(1));
        CCInfoTC = (data.get(2));
        CCInfoBS = (data.get(5) + "/" + data.get(3)); //BSRatio + BetSpread denominator
        CCInfoRB = (data.get(4));

    }


    //////////////////STAT COLLECTING////////////////////

    private void initializeStatMaps() {

        //instantiating the countFreqMap
        countFreqMap.put("<=-1", 0);
        countFreqMap.put("0", 0);
        countFreqMap.put("1", 0);
        countFreqMap.put("2", 0);
        countFreqMap.put("3", 0);
        countFreqMap.put("4", 0);
        countFreqMap.put("5", 0);
        countFreqMap.put("6", 0);
        countFreqMap.put("7", 0);
        countFreqMap.put("8", 0);
        countFreqMap.put("9", 0);
        countFreqMap.put("10", 0);
        countFreqMap.put("11", 0);
        countFreqMap.put("12", 0);
        countFreqMap.put("13", 0);
        countFreqMap.put("14", 0);
        countFreqMap.put("15", 0);
        countFreqMap.put("16", 0);
        countFreqMap.put("17", 0);
        countFreqMap.put("18", 0);
        countFreqMap.put(">=19", 0);

        //instantiating the countWLMap = KEY : ArrayList[WINS, LOSSES]
        countWLDMap.put("<=-1", new ArrayList<>() { {add(0); add(0); add(0);} });
        countWLDMap.put("0", new ArrayList<>() { {add(0); add(0); add(0);} });
        countWLDMap.put("1", new ArrayList<>() { {add(0); add(0); add(0);} });
        countWLDMap.put("2", new ArrayList<>() { {add(0); add(0); add(0);} });
        countWLDMap.put("3", new ArrayList<>() { {add(0); add(0); add(0);} });
        countWLDMap.put("4", new ArrayList<>() { {add(0); add(0); add(0);} });
        countWLDMap.put("5", new ArrayList<>() { {add(0); add(0); add(0);} });
        countWLDMap.put("6", new ArrayList<>() { {add(0); add(0); add(0);} });
        countWLDMap.put("7", new ArrayList<>() { {add(0); add(0); add(0);} });
        countWLDMap.put("8", new ArrayList<>() { {add(0); add(0); add(0);} });
        countWLDMap.put("9", new ArrayList<>() { {add(0); add(0); add(0);} });
        countWLDMap.put("10", new ArrayList<>() { {add(0); add(0); add(0);} });
        countWLDMap.put("11", new ArrayList<>() { {add(0); add(0); add(0);} });
        countWLDMap.put("12", new ArrayList<>() { {add(0); add(0); add(0);} });
        countWLDMap.put("13", new ArrayList<>() { {add(0); add(0); add(0);} });
        countWLDMap.put("14", new ArrayList<>() { {add(0); add(0); add(0);} });
        countWLDMap.put("15", new ArrayList<>() { {add(0); add(0); add(0);} });
        countWLDMap.put("16", new ArrayList<>() { {add(0); add(0); add(0);} });
        countWLDMap.put("17", new ArrayList<>() { {add(0); add(0); add(0);} });
        countWLDMap.put("18", new ArrayList<>() { {add(0); add(0); add(0);} });
        countWLDMap.put(">=19", new ArrayList<>() { {add(0); add(0); add(0);} });

    }

    public int calculateCount(Player player){

        updatePlayerInfo();
        updateCCInfo(player);


        if(player.getCardCountingMethodType().equals("True Count")){
            return Integer.parseInt(CCInfoTC);
        }
        else {
            return Integer.parseInt(CCInfoRC);
        }

    }

    public void countWinLoss(String WinLossDraw){

        //this is only to be incremented when the processing of the round results in a win or a loss, not a draw.

        updatePlayerInfo();
        updateCCInfo(player);
        int count = calculateCount(player);
        String adjustedCount;

        //System.out.println("True Count : " + CCInfoTC);
        //System.out.println("Running Count : " + CCInfoRC);
        //System.out.println("Calculated Count : " + count);

        if (count <= -1){
            adjustedCount = "<=-1";
        }
        else if (count >= 19){
            adjustedCount = ">=19";
        }
        else{
            adjustedCount = String.valueOf(count);
        }

        if(WinLossDraw == "WIN"){
            //incrementing win for count
            countWLDMap.get(adjustedCount).set(0, (countWLDMap.get(adjustedCount).get(0)+1));
        }
        else if (WinLossDraw == "LOSS"){
            //incrementing loss for count
            countWLDMap.get(adjustedCount).set(1, (countWLDMap.get(adjustedCount).get(1)+1));
        }
        else if (WinLossDraw == "DRAW"){
            //incrementing draw for count
            countWLDMap.get(adjustedCount).set(2, (countWLDMap.get(adjustedCount).get(2)+1));
        }

        //System.out.println(countWLDMap.toString());

    }

    public void incrementCountFreq(){

        //this is only to be incremented when the processing of the round result occurs in order to also compare Win/Loss ratio with count.

        updatePlayerInfo();
        updateCCInfo(player);

        int count = calculateCount(player);
        String adjustedCount;

        if (count <= -1){
            adjustedCount = "<=-1";
        }
        else if (count >= 19){
            adjustedCount = ">=19";
        }
        else{
            adjustedCount = String.valueOf(count);
        }

        //incrementing count frequency
        Integer freq = countFreqMap.get(adjustedCount);
        countFreqMap.put(adjustedCount, freq + 1);

    }

    public HashMap<String, Integer> getCountFreqMap(){
        //System.out.println("rounds played: " + roundsPlayed);
        return countFreqMap;
    }

    public HashMap<String, List<Integer>> getCountWLDMap(){
        return countWLDMap;
    }


}
