package Blackjack;

import java.util.ArrayList;

//for information visit https://www.gamblingsites.org/casino/blackjack/card-counting/hi-lo/

public class CCM_HighLowCount {
    int runningCount = 0;
    int trueCount = 0;

    double startingBankRoll;
    double minBet;
    double maxBet;
    //bet spread ratio "e.g. [1]/16, [1]/32 etc"
    int BSRatio = 1;

    int betSpread;

    CCM_HighLowCount(double startingBankRoll, double minBet, int betSpread) {
        this.startingBankRoll = startingBankRoll;
        this.minBet = minBet;
        this.maxBet = minBet;
        this.betSpread = betSpread;
    }

    public ArrayList<String> getAllData(){

        //the returned data will always be in form "Name, RC, TC, BS, MB/RB" across all CC classes
        ArrayList<String> data = new ArrayList<String>();
        data.add(getCCMethodName());
        data.add(String.valueOf(runningCount));
        data.add(String.valueOf(trueCount));
        data.add(String.valueOf(betSpread));
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

    public int getTrueCount(){
        return trueCount;
    }

    public String getCCMethodName(){
        return "High-Low Count";
    }

    public double getMaxBet(){
        return this.maxBet;
    }

    /* Method no longer valid, minBet now determined by bankRoll / bettingUnits
    public void setMinBet(float bankRoll){
        this.minBet = bankRoll / this.betSpread;
    }
    */

    //NOTE: cardSuit not used, but used for other CC methods (red-seven). This is to generify CC classes
    public void updateRunningCount(String cardName, String cardSuit, int decksInShoe){
        //System.out.println(cardName);
        if(cardName.contains("TEN") || cardName.contains("JACK") || cardName.contains("QUEEN") || cardName.contains("KING") || cardName.contains("ACE")){
            this.runningCount = this.runningCount - 1;
            //System.out.println("-1 = " + runningCount);
            updateBetSpread(decksInShoe);
        }
        else if(cardName.contains("TWO") || cardName.contains("THREE") || cardName.contains("FOUR") || cardName.contains("FIVE") || cardName.contains("SIX")){
            this.runningCount = this.runningCount + 1;
            //System.out.println("+1 = " + runningCount);
            updateBetSpread(decksInShoe);
        }
        //System.out.println("0 = " + runningCount);

    }

    private void updateTrueCount(int decksInShoe){
        trueCount = Math.round((float) runningCount/decksInShoe);
    }

    private void updateBetSpread(int decksInShoe){

        updateTrueCount(decksInShoe);

        maxBet = minBet;
        BSRatio = 1;

        //double tempBet = 0;

        if(trueCount >= 2){
            for(int i=1; i < trueCount; i++){
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


    public String deviation(int dealerValue, int handValue) {

        //basic strategy deviation <---- less risky version of "illustrious 18"

        if(handValue == 16 && dealerValue == 9 && runningCount > 4){
            return "STAND";
        }
        else if(handValue == 15 && dealerValue == 10 && runningCount > 3){
            return "STAND";
        }
        else if(handValue == 12 && dealerValue == 4 && runningCount < 0){
            return "HIT";
        }
        else if(handValue == 16 && dealerValue == 10 && runningCount > -1){
            return "STAND";
        }
        else if(handValue == 13 && dealerValue == 2 && runningCount < 0){
            return "HIT";
        }
        else if(handValue == 9 && dealerValue == 7 && runningCount > 2){
            return "DOUBLE DOWN";
        }
        else if(handValue == 10 && dealerValue == 10 && runningCount > 3){
            return "DOUBLE DOWN";
        }
        else if(handValue == 12 && dealerValue == 3 && runningCount > 1){
            return "STAND";
        }
        else if(handValue == 12 && dealerValue == 2 && runningCount > 2){
            return "STAND";
        }
        else{
            return null;
        }
    }


}

