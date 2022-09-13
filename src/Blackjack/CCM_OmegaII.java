package Blackjack;

import java.util.ArrayList;

//for more information visit https://www.gamblingsites.org/casino/blackjack/card-counting/omega-2/


                      //could implement playing deviations for this CCM in the future



public class CCM_OmegaII {

    int runningCount = 0;
    int trueCount = 0;

    double startingBankRoll;
    double minBet;
    double maxBet;
    //bet spread ratio "e.g. [1]/16, [1]/32 etc"
    int BSRatio = 1;

    int betSpread;

    CCM_OmegaII(double startingBankRoll, double minBet, int betSpread) {
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
        return "OmegaII";
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
        if(cardName.contains("TWO") || cardName.contains("THREE") || cardName.contains("SEVEN")){
            this.runningCount = this.runningCount + 1;
        }
        else if(cardName.contains("FOUR") || cardName.contains("FIVE") || cardName.contains("SIX")){
            this.runningCount = this.runningCount + 2;
        }
        else if(cardName.contains("NINE")){
            this.runningCount = this.runningCount - 1;
        }
        else if(cardName.contains("TEN") || cardName.contains("JACK") || cardName.contains("KING") || cardName.contains("QUEEN")){
            this.runningCount = this.runningCount - 2;
        }

        updateBetSpread(decksInShoe);

    }

    private void updateTrueCount(int decksInShoe){
        trueCount = Math.round((float) runningCount/decksInShoe);
    }

    private void updateBetSpread(int decksInShoe){

        updateTrueCount(decksInShoe);

        maxBet = minBet;
        BSRatio = 1;

        //double tempBet = 0;

        if(trueCount >= 1){
            for(int i=0; i < trueCount; i++){
                maxBet = maxBet + minBet;
                BSRatio = BSRatio + 1;
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
