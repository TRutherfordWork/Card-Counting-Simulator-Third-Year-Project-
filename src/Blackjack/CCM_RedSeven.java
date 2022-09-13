package Blackjack;

import java.util.ArrayList;

//for more information visit: https://www.casinoguardian.co.uk/blackjack/red-seven-blackjack-system/
//https://www.youtube.com/watch?v=LeQVdnk9rfY

//Same as HighLow, just with the addition of Red-Sevens contributing +1 to the running total & no true count conversion
//Imbalanced method as there the resultant count after all 52 cards will always be +2
//---> Therefore the running count must start at -2
//As there is no true count conversion, the player must subtract the starting running count by -2 for each deck in...
//...the shoe passed the first. (pseudo true-count)

//for single deck games: Negative count = 1 Unit, Zero = 2 Units, +2 = 4 Units STAY
//for shoe games       : Negative count = 1 Unit, 0->+5 = 2 Units, +6 = 3 Units then Units = Count/2

public class CCM_RedSeven {
    int runningCount = -2;

    double startingBankRoll;
    double minBet;
    double maxBet;
    //bet spread ratio "e.g. [1]/16, [1]/32 etc"
    int BSRatio = 1;

    int betSpread;

    CCM_RedSeven(double startingBankRoll, double minBet, int betSpread, int decksInShoe) {
        this.startingBankRoll = startingBankRoll;
        this.minBet = minBet;
        this.maxBet = minBet;
        this.betSpread = betSpread;

        calculateAdjustedRunningCount(decksInShoe);

    }

    public ArrayList<String> getAllData(){

        //the returned data will always be in form "Name, RC, TC, BS, MB/RB" across all CC classes
        ArrayList<String> data = new ArrayList<String>();
        data.add(getCCMethodName());
        data.add(String.valueOf(runningCount));
        data.add("N/A");
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

    public String getCCMethodName(){
        return "Red-Seven";
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

        if(cardName.contains("TEN") || cardName.contains("JACK") || cardName.contains("QUEEN") || cardName.contains("KING") || cardName.contains("ACE")){
            this.runningCount = this.runningCount - 1;
            updateBetSpread(decksInShoe);
        }
        else if(cardName.contains("TWO") || cardName.contains("THREE") || cardName.contains("FOUR") || cardName.contains("FIVE") || cardName.contains("SIX")){
            this.runningCount = this.runningCount + 1;
            updateBetSpread(decksInShoe);
        }
        //red-seven component
        else if(cardName.contains("SEVEN")){
            if(cardSuit.contains("HEARTS") || cardSuit.contains("DIAMONDS")){
                this.runningCount = this.runningCount + 1;
            }
        }

    }

    private void calculateAdjustedRunningCount(int decksInShoe){

        for(int i=1; i < decksInShoe; i++){
            runningCount = runningCount - 2;
            //System.out.println(runningCount);
        }
    }


    private void updateBetSpread(int decksInShoe){

        //for single deck games: Negative count = 1 Unit, Zero = 2 Units, +2 = 4 Units STAY
        //for shoe games       : Negative count = 1 Unit, 0->+5 = 2 Units, +6 = 3 Units then Units = Count/2

        maxBet = minBet;
        BSRatio = 1;

        //double tempBet = 0;
        if(decksInShoe == 1) {
            if (runningCount > -1 && runningCount < 2) {
                maxBet = maxBet * 2;
                BSRatio = 2;
            }
            else if(runningCount > 1){
                maxBet = maxBet * 4;
                BSRatio = 4;
            }
        }
        else if(decksInShoe > 1){
            if (runningCount > -1 && runningCount < 6){
                maxBet = maxBet * 2;
                BSRatio = 2;
            }
            else if (runningCount == 6){
                maxBet = maxBet * 3;
                BSRatio = 3;
            }
            else if (runningCount > 6){
                maxBet = maxBet * (Math.round((float) (runningCount / 2)));
                BSRatio = (Math.round((float) (runningCount / 2)));
                //added to maintain chosen betspreads
                if((runningCount/2) > betSpread){
                    maxBet = maxBet * betSpread;
                    BSRatio = betSpread;
                }
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
