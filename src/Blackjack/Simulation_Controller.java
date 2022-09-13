//as well as controlling the simulation fxml interactions, the class is also able to save and load data via json.


package Blackjack;

import com.google.gson.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.*;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.*;

public class Simulation_Controller {
    public Text progressionPercentage;
    public ProgressBar progressBar;
    public Text cardCountingMethodInfo;
    public Text decksInShoeInfo;
    public Text bankRollInfo;
    public Text betSpreadInfo;
    public Text minBetInfo;
    public Text gamesPlayedRatio;
    public Text roundsHourInfo;
    public Text hoursPerGameInfo;
    public Text minChipValueInfo;
    public ProgressIndicator loadingIndicator;
    public Text bettingUnitsInfo;
    public Button startSimulationButton;
    public Text deckPenetrationInfo;
    public TableColumn lossPercentCol;
    public TableColumn changeInWinCol;
    public Text roundsPlayedInfo;

    //Game Variables
    private Shoe shoe;
    private int shoeSize;
    private int originalShoeSize;
    private int originalShoeCardNum;
    private int minBet;
    private int bankRoll;
    private int RPH;
    private int HPG;
    private int minChipValue;
    private int betSpread;
    private int decksInShoe;
    private int numGames;

    //Game Variables To Be Updated By Thread Workers
    private Player templatePlayer;


    private int moneyWonPerRound;
    private int moneyLostPerRound;
    private int moneyWonPerHour;
    private int moneyLostPerHour;
    private int winsPerHour;
    private int lossPerHour;
    private int gamesRemaining;
    private int bettingUnits;


    private int deckPenetration;

    private int riskOfRuin;

    //manages thread execution
    private int threadsExecuted;

    //simulation results display widgets
    public TableView<SimulationStatCollector> countTable;
    public TableColumn countCol;
    public TableColumn frequencyCol;
    public TableColumn winCol;
    public TableColumn lossCol;
    public TableColumn drawCol;
    public TableColumn WLCol;
    //widgets to be filled after table data is compiled and computed
    public Text totalWinsInfo;
    public Text totalLossesInfo;
    public Text totalDrawsInfo;

    //Arraylists used to collect info across all rounds in game. To be returned to the simulation_controller
    private ArrayList<HashMap<String, Integer>> countFreqMap_GameResults = new ArrayList<>();
    private ArrayList<HashMap<String, List<Integer>>> countWLDMap_GameResults = new ArrayList<>();

    //constructing TreeMap to process card values from retrieved enum tokens
    private TreeMap<Card.Value, String> valueTreeMap = new TreeMap<>();

    //collects and averages data across games
    private HashMap<String, Double> bankTrackerAverager = new HashMap<>();


    //////////play-style variables///////
    private boolean wongingBool;


    public void initialise(Player player, int decksInShoe, int bankRoll, int betSpread, int minBet, int numGames, int RPH, int HPG, int minChipValue, int bettingUnits, int deckPenetration, boolean wongingBool){

        //setting values for user-entered information
        cardCountingMethodInfo.setText(player.getCardCountingMethodStats().get(0));
        decksInShoeInfo.setText(String.valueOf(decksInShoe));
        bankRollInfo.setText(String.valueOf(bankRoll));
        betSpreadInfo.setText("1/"+betSpread);
        minBetInfo.setText(String.valueOf(minBet));
        gamesPlayedRatio.setText("0/"+numGames);
        roundsHourInfo.setText(String.valueOf(RPH));
        hoursPerGameInfo.setText(String.valueOf(HPG));
        minChipValueInfo.setText(String.valueOf(minChipValue));
        bettingUnitsInfo.setText(String.valueOf(bettingUnits));
        deckPenetrationInfo.setText(String.valueOf(deckPenetration + "%"));

        //initialising game variables
        this.templatePlayer = player;
        this.shoe = new Shoe(decksInShoe);
        this.shoeSize = shoe.getShoeSize();
        this.originalShoeSize = this.shoeSize;
        this.originalShoeCardNum = this.shoeSize * 52;
        this.minBet = minBet;
        this.bankRoll = bankRoll;
        this.RPH = RPH;
        this.HPG = HPG;
        this.minChipValue = minChipValue;
        this.numGames = numGames;
        this.betSpread = betSpread;
        this.decksInShoe = decksInShoe;
        this.bettingUnits = bettingUnits;
        this.deckPenetration = deckPenetration;

        this.wongingBool = wongingBool;

        //initialising valueSet hashmap with corresponding tokens, Ace defined as 10 but will later be handled if need be)
        valueTreeMap.put(Card.Value.TWO, "2");
        valueTreeMap.put(Card.Value.THREE, "3");
        valueTreeMap.put(Card.Value.FOUR, "4");
        valueTreeMap.put(Card.Value.FIVE, "5");
        valueTreeMap.put(Card.Value.SIX, "6");
        valueTreeMap.put(Card.Value.SEVEN, "7");
        valueTreeMap.put(Card.Value.EIGHT, "8");
        valueTreeMap.put(Card.Value.NINE, "9");
        valueTreeMap.put(Card.Value.TEN, "10");
        valueTreeMap.put(Card.Value.JACK, "10");
        valueTreeMap.put(Card.Value.KING, "10");
        valueTreeMap.put(Card.Value.QUEEN, "10");
        valueTreeMap.put(Card.Value.ACE, "11");

        progressBar.setProgress(0.0);

        //populating bankTrackerAverager HashMap
        for(int game = 1; game < ((HPG * RPH)+1); game++){
            bankTrackerAverager.put(String.valueOf(game), 0.0);
        }


    }

    private void startSimulation(Player player){

        double newBankRoll = player.getBankRoll();
        String cardCountingMethod = player.getCardCountingMethodStats().get(0);

                                        //Original Single Threaded Code//
//        for(int i=0; i < numGames; i++) {
//
//            Player workerPlayer = new Player(newBankRoll, cardCountingMethod, betSpread, decksInShoe, bettingUnits);
//
//            BlackjackSimulation roundsSimulator = new BlackjackSimulation(shoe, valueTreeMap, workerPlayer, shoeSize, bankRoll, betSpread, minBet, RPH, HPG, deckPenetration);
//
//            gamesPlayedRatio.setText((i+1) + "/" + numGames);
//
//            System.out.println("Round Count Frequency Map: \n"+ roundsSimulator.getCountFreqMap().toString());
//            System.out.println("Round Count Win/Loss/Draw Map: \n" + roundsSimulator.getCountWLDMap().toString());
//
//            countFreqMap_GameResults.add(roundsSimulator.getCountFreqMap());
//            countWLDMap_GameResults.add(roundsSimulator.getCountWLDMap());
//
//        }

                        ///////////////multi-threading simulation code///////////////
        //the following code significantly speeds up the program's execution time via parallel programming over 100x!
        int threadNum = 12;

        int gamesToPlayPerThread = numGames / threadNum;

        for(int thread = 0; thread < threadNum; thread++){

            //instantiating worker to be runnable
            simulationThread worker = new simulationThread(thread, this, gamesToPlayPerThread, newBankRoll, cardCountingMethod,
                    betSpread, decksInShoe, bettingUnits, shoe, valueTreeMap, bankRoll, minBet, RPH, HPG, deckPenetration, wongingBool);

            //casting worker to thread to inherit all functions
            Thread workerThread = new Thread(worker);
            //starting work
            workerThread.start();

        }

        //waiting for all threads to complete
        while(threadsExecuted != threadNum){
            System.out.println("threadsExecuted" + threadsExecuted);
            System.out.println(threadsExecuted + "--------" + threadNum);
        }

        System.out.println("FINISHED!");
        progressionPercentage.setText("100%");
        progressBar.setProgress(100.0);
        gamesPlayedRatio.setText(numGames + "/" + numGames);
        roundsPlayedInfo.setText(String.valueOf(numGames * HPG * RPH));

        //resetting threads executed
        threadsExecuted = 0;


        //System.out.println(countFreqMap_GameResults.toString());
        HashMap<String, Float> averageFreqMap = averageFreqMap(countFreqMap_GameResults);
        System.out.println("Frequency Map Average: ");
        System.out.println(averageFreqMap.toString());

        HashMap<String, List<Double>> averageWLDMap = averageWLDMap(countWLDMap_GameResults);
        System.out.println(averageWLDMap.toString());

        //populating on-screen widgets with returned simulation results
        populateCountTable(averageFreqMap, averageWLDMap);


    }

    public void incrementThreadsExecuted(){
        threadsExecuted++;
    }

    public void addToResultsTables(BlackjackSimulation roundsSimulator){
        countFreqMap_GameResults.add(roundsSimulator.getCountFreqMap());
        countWLDMap_GameResults.add(roundsSimulator.getCountWLDMap());
    }

    public HashMap<String, List<Double>> averageWLDMap(ArrayList<HashMap<String, List<Integer>>> countWLDMap) {

        HashMap<String, Double> winsMap = new HashMap<>();
        HashMap<String, Double> lossesMap = new HashMap<>();
        HashMap<String, Double> drawsMap = new HashMap<>();

        List<String> keys = Arrays.asList("<=-1", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", ">=19");

        //populating new maps with keys
        for(String key : keys){
            winsMap.put(key, 0.);
            lossesMap.put(key, 0.);
            drawsMap.put(key, 0.);
        }


        for(HashMap<String, List<Integer>> map : countWLDMap){
            for(String key : keys){

                winsMap.put(key, (winsMap.get(key) + (float) map.get(key).get(0)));
                lossesMap.put(key, (lossesMap.get(key) + (float) map.get(key).get(1)));
                drawsMap.put(key, (drawsMap.get(key) + (float) map.get(key).get(2)));

            }
        }


        //averaging wins losses and draws
        Double denominator = (double) countWLDMap.size();

        for(String key : keys){

            winsMap.put(key, (winsMap.get(key) / denominator));
            lossesMap.put(key, (lossesMap.get(key) / denominator));
            drawsMap.put(key, (drawsMap.get(key) / denominator));

        }

        HashMap<String, List<Double>> averageWLDMap_GameResults = new HashMap<>();
        Double wins;
        Double losses;
        Double draws;

        //inserting winLossDraw maps into averageWLDMap
        for(String key : keys){

            wins = winsMap.get(key);
            losses = lossesMap.get(key);
            draws = drawsMap.get(key);

            Double[] WLD = {wins, losses, draws};

            averageWLDMap_GameResults.put(key, Arrays.asList(WLD));

        }

        return averageWLDMap_GameResults;

    }

    public HashMap<String, Float> averageFreqMap(ArrayList<HashMap<String, Integer>> countFreqMap){

        float temp_negative = 0, temp_zero = 0, temp_one = 0, temp_two = 0, temp_three = 0, temp_four = 0, temp_five = 0,
                temp_six = 0, temp_seven = 0, temp_eight = 0, temp_nine = 0, temp_ten = 0, temp_eleven = 0,
                    temp_twelve = 0, temp_thirteen = 0, temp_fourteen = 0, temp_fifteen = 0, temp_sixteen = 0,
                        temp_seventeen = 0, temp_eighteen = 0, temp_nineteen = 0;

        for(HashMap<String, Integer> map : countFreqMap){
            temp_negative += map.get("<=-1");
            temp_zero += map.get("0");
            temp_one += map.get("1");
            temp_two += map.get("2");
            temp_three += map.get("3");
            temp_four += map.get("4");
            temp_five += map.get("5");
            temp_six += map.get("6");
            temp_seven += map.get("7");
            temp_eight += map.get("8");
            temp_nine += map.get("9");
            temp_ten += map.get("10");
            temp_eleven += map.get("11");
            temp_twelve += map.get("12");
            temp_thirteen += map.get("13");
            temp_fourteen += map.get("14");
            temp_fifteen += map.get("15");
            temp_sixteen += map.get("16");
            temp_seventeen += map.get("17");
            temp_eighteen += map.get("18");
            temp_nineteen += map.get(">=19");

        }

        Float denominator = (float) countFreqMap.size();

        temp_negative = (temp_negative / denominator);
        temp_zero = (temp_zero / denominator);
        temp_one = (temp_one / denominator);
        temp_two = (temp_two / denominator);
        temp_three = (temp_three / denominator);
        temp_four = (temp_four / denominator);
        temp_five = (temp_five / denominator);
        temp_six = (temp_six / denominator);
        temp_seven = (temp_seven / denominator);
        temp_eight = (temp_eight / denominator);
        temp_nine = (temp_nine / denominator);
        temp_ten = (temp_ten / denominator);
        temp_eleven = (temp_eleven / denominator);
        temp_twelve = (temp_twelve / denominator);
        temp_thirteen = (temp_thirteen / denominator);
        temp_fourteen = (temp_fourteen / denominator);
        temp_fifteen = (temp_fifteen / denominator);
        temp_sixteen = (temp_sixteen / denominator);
        temp_seventeen = (temp_seventeen / denominator);
        temp_eighteen = (temp_eighteen / denominator);
        temp_nineteen = (temp_nineteen / denominator);


        HashMap<String, Float> averageFreqMap = new HashMap<>();

        averageFreqMap.put("<=-1", temp_negative);
        averageFreqMap.put("0", temp_zero);
        averageFreqMap.put("1", temp_one);
        averageFreqMap.put("2", temp_two);
        averageFreqMap.put("3", temp_three);
        averageFreqMap.put("4", temp_four);
        averageFreqMap.put("5", temp_five);
        averageFreqMap.put("6", temp_six);
        averageFreqMap.put("7", temp_seven);
        averageFreqMap.put("8", temp_eight);
        averageFreqMap.put("9", temp_nine);
        averageFreqMap.put("10", temp_ten);
        averageFreqMap.put("11", temp_eleven);
        averageFreqMap.put("12", temp_twelve);
        averageFreqMap.put("13", temp_thirteen);
        averageFreqMap.put("14", temp_fourteen);
        averageFreqMap.put("15", temp_fifteen);
        averageFreqMap.put("16", temp_sixteen);
        averageFreqMap.put("17", temp_seventeen);
        averageFreqMap.put("18", temp_eighteen);
        averageFreqMap.put(">=19", temp_nineteen);


        return averageFreqMap;

    }

    //////////////DISPLAYING SIMULATION RESULTS//////////////

    public void populateCountTable(HashMap<String, Float> averageFreqMap, HashMap<String, List<Double>> averageWLDMap){

        List<String> keys = Arrays.asList("<=-1", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", ">=19");

        ObservableList<SimulationStatCollector> statList = FXCollections.observableArrayList();

        for(String key : keys){

            List<Double> WLD = averageWLDMap.get(key);
            Float frequency = averageFreqMap.get(key);

            //calculating winPercentage
            double winLossPercentage = (WLD.get(0)/frequency)*100;
            String WLPercentage = new DecimalFormat("#.###").format(winLossPercentage);

            //calculating lossPercentage
            double LossPercentage = (WLD.get(1)/frequency)*100;
            String LPercentage = new DecimalFormat("#.###").format(LossPercentage);

            //calculating changeInWinPercentage (simulation returned standard win rate being 42.9% )
            double change = winLossPercentage - 42.9;
            String changeInWinPercentage = new DecimalFormat("#.###").format(change);

            //instantiating StatCollector object to then populate the table with
            statList.add(new SimulationStatCollector(key, frequency, WLD.get(0), WLD.get(1), WLD.get(2), WLPercentage, LPercentage, changeInWinPercentage));


        }

        //System.out.println(statList.toString());

        //setting value factory for each cell
        countCol.setCellValueFactory(new PropertyValueFactory<Simulation_Controller, String>("count"));
        frequencyCol.setCellValueFactory(new PropertyValueFactory<Simulation_Controller, String>("frequency"));
        winCol.setCellValueFactory(new PropertyValueFactory<Simulation_Controller, String>("win"));
        lossCol.setCellValueFactory(new PropertyValueFactory<Simulation_Controller, String>("loss"));
        drawCol.setCellValueFactory(new PropertyValueFactory<Simulation_Controller, String>("draw"));
        WLCol.setCellValueFactory(new PropertyValueFactory<Simulation_Controller, String>("WLPercentage"));
        lossPercentCol.setCellValueFactory(new PropertyValueFactory<Simulation_Controller, String>("LPercentage"));
        changeInWinCol.setCellValueFactory(new PropertyValueFactory<Simulation_Controller, String>("changeInWinPercentage"));


        countTable.setItems(statList);


        //summing table data to compute and display total wins/losses/draws
        Double totalWins = 0.0;
        Double totalLosses = 0.0;
        Double totalDraws = 0.0;
        for(SimulationStatCollector item : countTable.getItems()){
            totalWins = totalWins + Double.parseDouble(item.getWin());
            totalLosses = totalLosses + Double.parseDouble(item.getLoss());
            totalDraws = totalDraws + Double.parseDouble(item.getDraw());
        }

        //truncating information for display purposes
        String totalWinsFormatted = new DecimalFormat("#.###").format(totalWins);
        String totalLossesFormatted = new DecimalFormat("#.###").format(totalLosses);
        String totalDrawsFormatted = new DecimalFormat("#.###").format(totalDraws);


        totalWinsInfo.setText(totalWinsFormatted.toString());
        totalLossesInfo.setText(totalLossesFormatted.toString());
        totalDrawsInfo.setText(totalDrawsFormatted.toString());

    }

    public void startSimulationButtonAction(ActionEvent actionEvent) {
        //deactivating to prevent clicking the button whilst simulating
        startSimulationButton.setDisable(true);
        startSimulation(templatePlayer);
        startSimulationButton.setDisable(false);
    }


    /////////////////////////SAVING AND LOADING OF SAVE-FILES CODE//////////////////////////


    public void loadButtonAction(ActionEvent actionEvent) throws IOException {

        //retrieving saveFiles
        File folder = new File("SaveData");
        File[] listOfFiles = folder.listFiles();

        if(listOfFiles != null) {
            //loading fxml file with chosen parameters
            FXMLLoader loader = new FXMLLoader(getClass().getResource("fileLoader.fxml"));

            Parent popUpParent = loader.load();

            //controller method called to initialise combo box with equations list input
            fileLoader_Controller fileLoader_controller = loader.getController();
            fileLoader_controller.initialise(this, listOfFiles);

            Scene popUpScene = new Scene(popUpParent);

            Stage stage = new Stage();
            stage.setScene(popUpScene);

            stage.showAndWait();
        } else {

            System.out.println("No saveData available!");

        }

    }

    public void loadData(String fileName) throws IOException {
        String data = "";

        Scanner scanner = new Scanner(new File("SaveData/" + fileName));
        while(scanner.hasNext()){
            data = data + scanner.next();
        }

        scanner.close();
        System.out.println(data);

        String[] splitList = data.split("]");
        List<List<String>> dataList = new ArrayList<>();

        for(String list : splitList){

            list = list.replace("[", "");
            list = list.replace("]", "");
            //converting uniCode
            list = list.replace("\\u003c\\u003d", "<=");
            list = list.replace("\\u003e\\u003d", ">=");
            list = list.replace("\"", "");

            dataList.add(Arrays.asList(list.split(",")));

        }

        System.out.println(dataList);

        //rebuilding array to perform total conversion of JSON to arraylist
        List<List<String>> newList = new ArrayList<>();
        List<String> temp = new ArrayList<>();

        for(List<String> list : dataList){
            temp = new ArrayList<>();
            for(String attribute : list){
               if(attribute != ""){
                   temp.add(attribute);
               }
            }
            newList.add(temp);
        }

        System.out.println(newList);

        //pushing SaveData into the tableView
        ObservableList<SimulationStatCollector> statList = FXCollections.observableArrayList();

        for(List<String> stats : newList){
            //calculating changeInWinPercentage (simulation returned standard win rate being 42.9% )
            double change = Double.parseDouble(stats.get(5)) - 42.9;
            String changeInWinPercentage = new DecimalFormat("#.###").format(change);
            //reconstructing the SimulationStatCollector objects to be displayed from the SaveData
            statList.add(new SimulationStatCollector(stats.get(0), (float) Double.parseDouble(stats.get(1)), Double.parseDouble(stats.get(2)),
                        Double.parseDouble(stats.get(3)), Double.parseDouble(stats.get(4)), stats.get(5), stats.get(6), changeInWinPercentage));
        }

        countCol.setCellValueFactory(new PropertyValueFactory<Simulation_Controller, String>("count"));
        frequencyCol.setCellValueFactory(new PropertyValueFactory<Simulation_Controller, String>("frequency"));
        winCol.setCellValueFactory(new PropertyValueFactory<Simulation_Controller, String>("win"));
        lossCol.setCellValueFactory(new PropertyValueFactory<Simulation_Controller, String>("loss"));
        drawCol.setCellValueFactory(new PropertyValueFactory<Simulation_Controller, String>("draw"));
        WLCol.setCellValueFactory(new PropertyValueFactory<Simulation_Controller, String>("WLPercentage"));
        lossPercentCol.setCellValueFactory(new PropertyValueFactory<Simulation_Controller, String>("LPercentage"));
        changeInWinCol.setCellValueFactory(new PropertyValueFactory<Simulation_Controller, String>("changeInWinPercentage"));

        countTable.setItems(statList);

    }

    public void saveButtonAction(ActionEvent actionEvent) throws IOException {

        SimulationStatCollector extractedObject;
        //extracting all information from tableView and casting to SimulationStatCollector objects to in JSON
        List<List<String>> saveList = new ArrayList<>();

        for(int i = 0; i < countTable.getItems().size(); i++){
            extractedObject = countTable.getItems().get(i);
            saveList.add(extractedObject.getAllData());
        }

        //building GSON object for saving to JSON
        Gson gsonData = new Gson();
        String jsonData = gsonData.toJson(saveList);

        //building new file
        List<String> fileNames = getSaveFileNames();
        //setting name of file
        String newFileName = this.cardCountingMethodInfo.getText();

        int counter = 0;
        for(String fileName : fileNames){

            if(fileName.contains(newFileName)){counter++;}

        }
        if(counter != 0){
            newFileName = newFileName + "(" + counter + ")" + " (DEMO)";
        }


        File file1 = new File("SaveData/" + newFileName + ".JSON");
        FileWriter fw = new FileWriter(file1);
        PrintWriter pw = new PrintWriter(fw);

        pw.write(jsonData);
        pw.close();

    }

    private List<String> getSaveFileNames(){

        List<String> fileNames = new ArrayList<>();

        File folder = new File("SaveData");
        File[] listOfFiles = folder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
            fileNames.add(listOfFiles[i].getName());
        }


        return fileNames;

    }


    ////////////////////////BANKROLL INFORMATION HANDLER/////////////////////////
    public void addToBankRollResults(SimulationBankTracker bankTracker) {

        Double oldBankRoll;
        Double newBankRoll;

        for(int i=1; i < (HPG * RPH)+1; i++){
            oldBankRoll = bankTrackerAverager.get(String.valueOf(i));
            newBankRoll = bankTracker.getData().get(String.valueOf(i));
            //averaging the two
            newBankRoll = (newBankRoll + oldBankRoll) / 2;
            //setting average for round
            bankTrackerAverager.put(String.valueOf(i), newBankRoll);
            //resetting bankTracker for round
            bankTracker.bankTracker.put(String.valueOf(i), 0.0);
        }

    }



    /////////////////////////RESULTS VISUALISATION CODE//////////////////////////



    public void visualiseButtonAction(ActionEvent actionEvent) throws IOException {

        //obtaining table data
        SimulationStatCollector extractedObject;
        List<SimulationStatCollector> tableData = new ArrayList<>();

        for(int i = 0; i < countTable.getItems().size(); i++){
            extractedObject = countTable.getItems().get(i);
            tableData.add(extractedObject);
        }

        //loading fxml file with chosen parameters
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Visualisation.fxml"));

        Parent popUpParent = loader.load();

        //controller method called to initialise combo box with equations list input
        Visualisation_Controller visualisation_controller = loader.getController();
        visualisation_controller.initialise(tableData);

        Scene popUpScene = new Scene(popUpParent);

        Stage stage = new Stage();
        stage.setScene(popUpScene);

        stage.showAndWait();

        System.out.println(bankTrackerAverager.toString());


    }

}
