package Blackjack;

//no true count conversion

//Ace is equal to -1 on true count
//Five is equal to +1 on true count

//Raise bet for every +2 on running count
//Below 2 always bet the minimum

//no deviation from basic strategy

//more info https://www.youtube.com/watch?v=t1xjiztJ9vQ&t=42s

import java.util.ArrayList;

public class CCM_AceFiveCount {

    int runningCount = 0;

    double startingBankRoll;
    double minBet;
    double maxBet;
    //bet spread ratio "e.g. [1]/16, [1]/32 etc"
    int BSRatio = 1;

    int betSpread;

    CCM_AceFiveCount(double startingBankRoll, double minBet, int betSpread) {
        //NOTE: decks in shoe not used, but used for other CC methods. This is to generify CC classes

        this.startingBankRoll = startingBankRoll;
        this.minBet = minBet;
        this.maxBet = minBet;
        this.betSpread = betSpread;
    }

    public ArrayList<String> getAllData(){

        //the returned data will always be in form "Name, RC, TC, BS, MB/RB" across all CC classes
        ArrayList<String> data = new ArrayList<String>();
        data.add(getCCMethodName());
        data.add(String.valueOf(getRunningCount()));
        data.add("N/A");
        data.add(String.valueOf(getBetSpread()));
        data.add(String.valueOf(Math.round(getMaxBet())));
        data.add(String.valueOf(BSRatio));

        return data;
    }

    public double getBetSpread(){
        return this.betSpread;
    }

    public int getRunningCount(){
        return runningCount;
    }

    public String getCCMethodName(){
        return "Ace-Five Count";
    }

    public double getMaxBet(){
        return this.maxBet;
    }

    /* Method no longer valid, minBet now determined by bankRoll / bettingUnits
    public void setMinBet(float bankRoll){
        this.minBet = bankRoll / this.betSpread;
    }
    */

    //NOTE: despite decksInShoe & cardSuit not being used here, it is in other CC methods and thus is required to
    //...generify the program in the implementation
    public void updateRunningCount(String cardName, String cardSuit, int decksInShoe){
        if(cardName.contains("ACE")){
            this.runningCount = this.runningCount - 1;
            updateBetSpread();
        }
        else if(cardName.contains("FIVE")){
            this.runningCount = this.runningCount + 1;
            updateBetSpread();
        }

    }

    private void updateBetSpread(){

        maxBet = minBet;
        BSRatio = 1;

        if(runningCount >= 2){
            for(int i=1; i < runningCount; i++){
                maxBet = maxBet * 2;
                BSRatio = BSRatio * 2;
                if(BSRatio == betSpread){break;}
            }
        }

    }

    public void printAllStats(){

        System.out.println("Running Count : " + runningCount + "\n"
                            + "Starting Bankroll : " + startingBankRoll + "\n"
                            + "BetSpread : " + betSpread + "\n"
                            + "Max Bet : "   + maxBet + "\n"
                            + "Min Bet : "   + minBet + "\n");

    }


}
