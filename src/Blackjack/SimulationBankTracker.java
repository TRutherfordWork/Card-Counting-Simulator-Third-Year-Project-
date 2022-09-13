package Blackjack;

import java.util.HashMap;

public class SimulationBankTracker {

    private Double startingBankRoll;
    private int roundsNum;
    //keeps track of bankRoll throughout the game simulation (made up of x rounds)
    public HashMap<String, Double> bankTracker = new HashMap<>();

    public SimulationBankTracker(Double startingBankRoll, int roundsNum){
        this.startingBankRoll = startingBankRoll;
        this.roundsNum = roundsNum;

        //populating HashMaps
        for(int game = 1; game < (roundsNum+1); game++){
            bankTracker.put(String.valueOf(game), 0.0);
        }


        //setting first game bankRoll equal to startingBankRoll
        bankTracker.put("1", startingBankRoll);

    }

    public void addRoundData(int roundNum, Double bankRoll){
        bankTracker.put(String.valueOf(roundNum), bankRoll);
    }

    public HashMap<String, Double> getData(){
        return bankTracker;
    }

    public void printInfo(){
        for(int i=1; i < roundsNum+1; i++){
            Double bankRoll = bankTracker.get(String.valueOf(i));
            System.out.println("Game: " + i + "    BankRoll: " + bankRoll);
        }
    }

}
