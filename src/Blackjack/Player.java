package Blackjack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class Player {

    private double bankRoll;
    private String cardCountingMethod;
    //needs to be public as it is accessed in case of splitHands
    public Hand playerHand;

    //Card Counting related
    private CCM_AceFiveCount AceFiveCount;
    private CCM_HighLowCount HighLowCount;
    private CCM_RedSeven RedSeven;
    private CCM_KnockOut KnockOut;
    private CCM_ZenCount ZenCount;
    private CCM_OmegaII OmegaII;
    private CCM_None noCardCountingMethod;

    private int betSpread;
    private double minBet;
    private int bettingUnits;
    private int decksInShoe;
    //

    //split hands code
    boolean splitHand = false;
    ArrayList<Hand> splitHandsArrayList = new ArrayList<>();

    public Player(double bankRoll, String cardCountingMethod, int betSpread, int decksInShoe, int bettingUnits){
        this.bankRoll = bankRoll;
        this.cardCountingMethod = cardCountingMethod;
        this.playerHand = new Hand();
        this.betSpread = betSpread;
        this.minBet = bankRoll / bettingUnits;
        this.decksInShoe = decksInShoe;

        setCardCountingMethod(decksInShoe);

    }

    public Player(){
        this.bankRoll = 10000;
    }

    public double getBankRoll(){
        return this.bankRoll;
    }

    public void setBankRoll(double newBankRoll){
        this.bankRoll = newBankRoll;
    }

    public void bet(double bet){
        this.bankRoll -= bet;
    }

    public void addFunds(double funds){
        this.bankRoll += funds;
    }

    public Hand getPlayerHand(){
        return this.playerHand;
    }

    public void clearPlayerHand(){
        this.playerHand = new Hand();
    }

    public int getPlayerHandValue() {
        return this.playerHand.getValue();
    }

    //implementing basic strategy
    //will a String telling the system which is the best option for the player (based of BStrat)
    //see Literature Review for Rules of Basic Strategy
    public String getBasicStrategyDecision(int dealersKnownValue) {
        int handValue = getPlayerHandValue();

        boolean aceConversion = playerHand.aceConversion();

        String bestDecision;

        //the decision is based upon not only the added value of the cards, but which individual cards are
        //in the player's hand. Therefore the system needs to scan through the card names and make decisions

        String handListString = this.playerHand.toString();
        //converting handListString to arrayList
        handListString = handListString.substring(1,handListString.length()-1);
        List<String> handList = new ArrayList<>(Arrays.asList(handListString.split(", ")));
        //System.out.println(handList.toString());
        //converting handList into hashSet (to detect doubles -> split) (need to remove OF X text as a double is detected from value, not card name)
        String tempString = "";

        for(String cardName : handList){
            //System.out.println(cardName);
            List<String> tempCardName = new ArrayList<>(Arrays.asList(cardName.split(" ")));
            tempString = tempString + (tempCardName.get(0) + " ");
        }

        List<String> valueStringList = new ArrayList<>(Arrays.asList(tempString.split(" ")));

        //System.out.println(valueStringList);

        HashSet<String> hashSet = new HashSet<>(valueStringList);

        //making decisions based upon hand following BStrat
        if((handList.size() != hashSet.size()) && (handList.size() < 3)){
            //System.out.println("this is a double hand");
            //System.out.println(handList);
            bestDecision = doubleHand(dealersKnownValue, valueStringList);
        }
        else if((handList.toString().contains("ACE OF DIAMONDS") || handList.toString().contains("ACE OF SPADES") || handList.toString().contains("ACE OF HEARTS") || handList.toString().contains("ACE OF CLUBS")) && (!aceConversion)){
            bestDecision = aceHand(dealersKnownValue);
        }
        else{
            bestDecision = standardHand(dealersKnownValue);
        }

        //handling event where there are three cards in hand, DOUBLE DOWN should be HIT
        if(handList.size() > 2 && bestDecision.equals("DOUBLE DOWN")){
            bestDecision = "HIT";
        }

        return bestDecision;
    }

    //HARD HAND (no ace)
    private String standardHand(int dealersKnownValue){
        int handValue = getPlayerHandValue();

        //System.out.println("handvalue = " + handValue);
        //System.out.println("dealersvalue = " + dealersKnownValue);


        if (handValue < 9){
            return "HIT";
        }
        else if (handValue == 9){

            if(dealersKnownValue == 2){
                return "HIT";
            }
            else if(dealersKnownValue < 7){
                return "DOUBLE DOWN";
            }
            else{
                return "HIT";
            }

        }
        else if (handValue == 10){

            if(dealersKnownValue < 10){
                return "DOUBLE DOWN";
            }
            else{
                return "HIT";
            }

        }
        else if (handValue == 11){

            if(dealersKnownValue < 11){
                return "DOUBLE DOWN";
            }
            else{
                return "HIT";
            }

        }
        else if (handValue == 12){

            if(dealersKnownValue < 4){
                return "HIT";
            }
            else if(dealersKnownValue < 7){
                return "STAND";
            }
            else{
                return "HIT";
            }

        }
        else if ((handValue == 13) || (handValue == 14) || (handValue == 15) || (handValue == 16)){
           //("recognised handvalue as 13,14,15 or 16");

            if(dealersKnownValue < 7){
               //System.out.println("recognised dealers value as less than 7");
                return "STAND";
            }
            else{
                //System.out.println("recognised dealers value as greater than 7");
                return "HIT";
            }

        }
        else{
            return "STAND";
        }
    }

    //SPLIT HAND
    private String doubleHand(int dealersKnownValue, List<String> valueStringList){
        int handValue = getPlayerHandValue();

        //System.out.println("handvalue = " + handValue);
        //System.out.println("dealersvalue = " + dealersKnownValue);

        if(handValue == 20){

            return "STAND";

        }
        else if(handValue == 18){

            if(dealersKnownValue < 7){
                return "SPLIT";
            }
            else if(dealersKnownValue == 7){
                return "STAND";
            }
            else if(dealersKnownValue < 10){
                return "SPLIT";
            }
            else{
                return "STAND";
            }

        }
        else if(handValue == 16){

            return "SPLIT";

        }
        else if(handValue == 14){

            if(dealersKnownValue < 8){
                return "SPLIT";
            }
            else{
                return "HIT";
            }

        }
        else if(handValue == 12){

            if(dealersKnownValue == 2){
                return "HIT";
            }
            else if(dealersKnownValue < 7){
                return "SPLIT";
            }
            else{
                return "HIT";
            }

        }
        else if(handValue == 8){
            return "HIT";
        }
        else if(handValue == 6 || handValue == 4){

            if(dealersKnownValue < 4){
                return "HIT";
            }
            if(dealersKnownValue < 8){
                return "SPLIT";
            }
            else{
                return "HIT";
            }

        }
        else{
            return "HIT";
        }

    }

    //SOFT HAND (an ace that can be counted as 11)
    private String aceHand(int dealersKnownValue){
        int handValue = getPlayerHandValue();
        //System.out.println("handvalue = " + handValue);
        //System.out.println("dealersvalue = " + dealersKnownValue);

        if(handValue == 13 || handValue == 14){

            if(dealersKnownValue < 5){
                return "HIT";
            }
            else if(dealersKnownValue < 7){
                return "DOUBLE DOWN";
            }
            else{
                return "HIT";
            }

        }
        else if(handValue == 15 || handValue == 16){

            if(dealersKnownValue < 4){
                return "HIT";
            }
            else if(dealersKnownValue < 7){
                return "DOUBLE DOWN";
            }
            else{
                return "HIT";
            }

        }
        else if(handValue == 17){

            if(dealersKnownValue < 3){
                return "HIT";
            }
            else if(dealersKnownValue < 7){
                return "DOUBLE DOWN";
            }
            else{
                return "HIT";
            }

        }
        else if(handValue == 18){

            if(dealersKnownValue == 2){
                return "STAND";
            }
            else if(dealersKnownValue < 7){
                return "DOUBLE DOWN";
            }
            else if(dealersKnownValue < 9){
                return "STAND";
            }
            else{
                return "HIT";
            }

        }
        else{
            return "STAND";
        }

    }

    ////////////Setting Split Hand implementation//////////////
    public void splitHand() {

        splitHand = true;

        //splitting the hands
        Card firstCard = this.playerHand.getCard(0);
        Card secondCard = this.playerHand.getCard(1);

        //creating two separate hands and appending splitHand arrayList (draws will occur in simulation code)
        Hand firstHand = new Hand();
        Hand secondHand = new Hand();
        firstHand.addCard(firstCard);
        secondHand.addCard(secondCard);

        splitHandsArrayList.add(firstHand);
        splitHandsArrayList.add(secondHand);

    }

    public void resetPlayerSplit(){

        splitHandsArrayList = new ArrayList<>();

    }


    ///////////card counting method implementation/////////////


    private void setCardCountingMethod(int decksInShoe){
        if(this.cardCountingMethod == "Ace-Five Count"){
            this.AceFiveCount = new CCM_AceFiveCount(this.bankRoll, this.minBet, this.betSpread);
        }
        else if(this.cardCountingMethod == "High-Low Count"){
            this.HighLowCount = new CCM_HighLowCount(this.bankRoll, this.minBet, this.betSpread);
        }
        else if(this.cardCountingMethod == "Red-Seven"){
            this.RedSeven = new CCM_RedSeven(this.bankRoll, this.minBet, this.betSpread, decksInShoe);
        }
        else if(this.cardCountingMethod == "Knock-Out"){
            this.KnockOut = new CCM_KnockOut(this.bankRoll, this.minBet, this.betSpread, decksInShoe);
        }
        else if(this.cardCountingMethod == "Zen Count"){
            this.ZenCount = new CCM_ZenCount(this.bankRoll, this.minBet, this.betSpread);
        }
        else if(this.cardCountingMethod == "OmegaII"){
            this.OmegaII = new CCM_OmegaII(this.bankRoll, this.minBet, this.betSpread);
        }
        else if(this.cardCountingMethod == "None"){
            this.noCardCountingMethod = new CCM_None(this.bankRoll, this.minBet);
        }

    }

    public void cardCountingMethodEvent(String cardName, String cardValue, int decksInShoe){
        if(this.cardCountingMethod == "Ace-Five Count"){
            this.AceFiveCount.updateRunningCount(cardName, cardValue, decksInShoe);
            //System.out.println("CC updated! - AceFiveCount");
        }
        else if(this.cardCountingMethod == "High-Low Count"){
            this.HighLowCount.updateRunningCount(cardName, cardValue, decksInShoe);
            //System.out.println("CC updated! - HighLowCount");
        }
        else if(this.cardCountingMethod == "Red-Seven"){
            this.RedSeven.updateRunningCount(cardName, cardValue, decksInShoe);
            //System.out.println("CC updated! - RedSeven");
        }
        else if(this.cardCountingMethod == "Knock-Out"){
            this.KnockOut.updateRunningCount(cardName, cardValue, decksInShoe);
        }
        else if(this.cardCountingMethod == "Zen Count"){
            this.ZenCount.updateRunningCount(cardName, cardValue, decksInShoe);
        }
        else if(this.cardCountingMethod == "OmegaII"){
            this.OmegaII.updateRunningCount(cardName, cardValue, decksInShoe);
        }
        else if(this.cardCountingMethod == "None"){
            this.noCardCountingMethod.updateRunningCount(cardName, cardValue, decksInShoe);
        }

    }


    public ArrayList<String> getCardCountingMethodStats() {

        ArrayList<String> data = new ArrayList<String>();

        if (this.cardCountingMethod == "Ace-Five Count") {
            data = this.AceFiveCount.getAllData();
        }
        else if(this.cardCountingMethod == "High-Low Count"){
            data = this.HighLowCount.getAllData();
        }
        else if(this.cardCountingMethod == "Red-Seven"){
            data = this.RedSeven.getAllData();
        }
        else if(this.cardCountingMethod == "Knock-Out"){
            data = this.KnockOut.getAllData();
        }
        else if(this.cardCountingMethod == "Zen Count"){
            data = this.ZenCount.getAllData();
        }
        else if(this.cardCountingMethod == "OmegaII"){
            data = this.OmegaII.getAllData();
        }
        else if(this.cardCountingMethod == "None"){
            data = this.noCardCountingMethod.getAllData();
        }

        return data;
    }

    public void resetCardCountingMethod(){
        setCardCountingMethod(this.decksInShoe);
    }


    //card counting basic strategy deviation

    public String getCardCountingDeviationDecision(int dealerValue) {

        int handValue = this.getPlayerHandValue();
        String decision = null;

        if(this.cardCountingMethod == "High-Low Count"){
            decision = this.HighLowCount.deviation(dealerValue, handValue);
            //System.out.println("High-Low Count deviation!");
        }

        return decision;

    }

    public String getCardCountingMethodType() {

        if (this.cardCountingMethod == "Ace-Five Count") {
            return "Running Count";
        }
        else if(this.cardCountingMethod == "High-Low Count"){
            return "True Count";
        }
        else if(this.cardCountingMethod == "Red-Seven"){
            return "Running Count";
        }
        else if(this.cardCountingMethod == "Knock-Out"){
            return "Running Count";
        }
        else if(this.cardCountingMethod == "Zen Count"){
            return "Running Count";
        }
        else if(this.cardCountingMethod == "OmegeII"){
            return "Running Count";
        }
        else{
            return "Running Count";
        }

    }
}
