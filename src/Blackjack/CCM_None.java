package Blackjack;

//this will be instantiated if the No Card Counting Method is selected.
//this is used as error prevention and is necessary in order to run a "vanilla" game

import java.util.ArrayList;


public class CCM_None {

    private double bankRoll;
    private double minBet;


    CCM_None(double bankRoll, double minBet) {
        this.bankRoll = bankRoll;
        this.minBet = minBet;
    }

    public ArrayList<String> getAllData(){

        //returns blank data
        ArrayList<String> data = new ArrayList<String>();
        data.add(getCCMethodName());
        data.add(String.valueOf(0)); //running count is always 0
        data.add("N/A");
        data.add(String.valueOf(1)); //bet spread is always 1/1
        data.add(String.valueOf(minBet)); //maxbet will always equal minBet
        data.add(String.valueOf(1)); //bet spread ratio

        return data;
    }

    public String getCCMethodName(){
        return "None";
    }


    /* Method no longer valid, minBet now determined by bankRoll / bettingUnits
    public void setMinBet(float bankRoll){
        this.minBet = bankRoll / this.betSpread;
    }
    */

    //NOTE: despite decksInShoe & cardSuit not being used here, it is in other CC methods and thus is required to
    //...generify the program in the implementation
    public void updateRunningCount(String cardName, String cardSuit, int decksInShoe){
    }

    private void updateBetSpread(){
    }

    public void printAllStats(){

        System.out.println("Running Count : " + "N/A" + "\n"
                + "Starting Bankroll : " + bankRoll + "\n"
                + "BetSpread : " + "N/A" + "\n"
                + "Max Bet : "   + minBet + "\n"
                + "Min Bet : "   + minBet + "\n");

    }




}
