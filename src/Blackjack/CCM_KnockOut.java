package Blackjack;

import java.util.ArrayList;

//Cards 2-7 -> +1 running count (low cards)
//Cards 8&9 -> +0 running count (neutral cards)
//Cards 10-A -> -1 running count (high cards)

//No true count

//1 deck -> running count starts at 0
//2 deck -> running count starts at -4
//6 deck -> running count starts at -20
//8 deck -> running count starts at -28

/**

 REMINDER: this method does not follow conventional betSpreads, and forces its own.
            could be an option for further testing

 for shoes less than 6    -> MaxBet = Maximum of 6 betting units (no matter bet spread)
 for shoes greater than 6 -> MaxBet = Maximum of 12 betting units (no matter bet spread)

 **/

public class CCM_KnockOut {

    int runningCount;

    double startingBankRoll;
    double minBet;
    double maxBet;
    //bet spread ratio "e.g. [1]/16, [1]/32 etc"
    int BSRatio = 1;
    int decksInShoe;

    int betSpread;

    CCM_KnockOut(double startingBankRoll, double minBet, int betSpread, int decksInShoe) {
        this.startingBankRoll = startingBankRoll;
        this.minBet = minBet;
        this.maxBet = minBet;
        this.betSpread = betSpread;

        this.decksInShoe = decksInShoe;

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
        return "Knock-Out";
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
            //System.out.println(cardName + ":  runningcount - 1");
            this.runningCount = this.runningCount - 1;
            updateBetSpread(decksInShoe);
        }
        else if(cardName.contains("TWO") || cardName.contains("THREE") || cardName.contains("FOUR") || cardName.contains("FIVE") || cardName.contains("SIX") || cardName.contains("SEVEN")){
            //System.out.println(cardName + ":  runningcount + 1");
            this.runningCount = this.runningCount + 1;
            updateBetSpread(decksInShoe);
        }

    }

    private void calculateAdjustedRunningCount(int decksInShoe){

        switch(decksInShoe){
            case 0, 1       -> runningCount = 0;
            case 2, 3, 4, 5 -> runningCount = -4;
            case 6, 7       -> runningCount = -20;
            case 8          -> runningCount = -28;
        }


    }


    private void updateBetSpread(int decksInShoe) {

        //for single deck games: Negative count = 1 Unit, Zero = 2 Units, +2 = 4 Units STAY
        //for shoe games       : Negative count = 1 Unit, 0->+5 = 2 Units, +6 = 3 Units then Units = Count/2

        maxBet = minBet;
        BSRatio = 1;

        //double tempBet = 0;
        if (decksInShoe < 6) {
            if (runningCount > 0 && runningCount < 6) {
                maxBet = maxBet * (runningCount + 1);
                BSRatio = BSRatio * (runningCount + 1);
            }
            if (runningCount > 5) {
                maxBet = maxBet * 6;
                BSRatio = BSRatio * 6;
            }
        } else {
            switch (runningCount){

                case 0 -> maxBet = minBet;
                case 1 -> maxBet = maxBet * 2;
                case 2 -> maxBet = maxBet * 4;
                case 3 -> maxBet = maxBet * 8;
                case 4 -> maxBet = maxBet * 10;
                case 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16 -> maxBet = maxBet * 12;

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
