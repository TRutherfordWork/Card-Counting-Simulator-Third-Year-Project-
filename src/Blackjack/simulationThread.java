package Blackjack;

//this class will allow the simulation process to fully execute much faster.
//delivering results quicker, allowing for more simulations to occur in the same amount of time simultaneously
//therefore allowing for more accurate results in the same amount of time as non multi-threaded simulation execution

import java.util.TreeMap;

public class simulationThread implements Runnable {

    int threadNum;

    Simulation_Controller controller;

    int gamesToBePlayed;
    double newBankRoll;
    String cardCountingMethod;
    int betSpread;
    int decksInShoe;
    int bettingUnits;
    Shoe shoe;
    TreeMap<Card.Value, String> cardValues;
    int bankRoll;
    int minBet;
    //rounds per hour
    int RPH;
    //hours per game
    int HPG;
    int targetDeckPenetration;

    //play-style variables
    boolean wongingBool;


    simulationThread(int threadNum, Simulation_Controller controller, int gamesToBePlayed, double newBankRoll, String cardCountingMethod, int betSpread, int decksInShoe,
                     int bettingUnits, Shoe shoe, TreeMap<Card.Value, String> cardValues, int bankRoll, int minBet, int RPH, int HPG, int targetDeckPenetration, boolean wongingBool){

        this.threadNum = threadNum;

        this.controller = controller;
        this.gamesToBePlayed = gamesToBePlayed;
        this.newBankRoll = newBankRoll;
        this.cardCountingMethod = cardCountingMethod;
        this.betSpread = betSpread;
        this.decksInShoe = decksInShoe;
        this.bettingUnits = bettingUnits;
        this.shoe = shoe;
        this.cardValues = cardValues;
        this.bankRoll = bankRoll;
        this.minBet = minBet;
        this.RPH = RPH;
        this.HPG = HPG;
        this.targetDeckPenetration = targetDeckPenetration;
        this.wongingBool = wongingBool;

    }

    @Override
    public void run() {

        for(int i = 0; i < gamesToBePlayed; i++){
            Player workerPlayer = new Player(newBankRoll, cardCountingMethod, betSpread, decksInShoe, bettingUnits);

            //instantiating bankTracker to track bankRoll data throughout rounds
            SimulationBankTracker bankTracker = new SimulationBankTracker((double) bankRoll, (RPH * HPG));

            BlackjackSimulation roundsSimulator = new BlackjackSimulation(shoe, cardValues, workerPlayer, shoe.getShoeSize(), bankRoll, betSpread, minBet, RPH, HPG, targetDeckPenetration, bankTracker, wongingBool);

            if(roundsSimulator.gameOver == true){
                this.controller.incrementThreadsExecuted();
            }

            addResults(roundsSimulator, bankTracker);
        }

        finished();

    }

    synchronized void addResults(BlackjackSimulation roundsSimulator, SimulationBankTracker bankTracker){
        this.controller.addToResultsTables(roundsSimulator);
        this.controller.addToBankRollResults(bankTracker);
    }

    synchronized void finished(){
        this.controller.incrementThreadsExecuted();
    }


}
