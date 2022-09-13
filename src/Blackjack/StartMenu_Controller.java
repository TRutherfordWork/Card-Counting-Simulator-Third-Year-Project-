package Blackjack;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;

public class StartMenu_Controller {
    public ComboBox playersCombo;
    public ComboBox decksCombo;
    public TextField bankrollTextField;
    public TextField minBetTextField;
    public Button playButton;
    public ComboBox CardCountingMethodsCombo;
    public ComboBox betSpreadCombo;
    public int minBet;
    //simulation widgets
    public ComboBox simPlayersCombo;
    public ComboBox simDecksCombo;
    public TextField simBankrollTextField;
    public TextField simMinBetTextField;
    public Button simulateButton;
    public ComboBox simCardCountingMethodsCombo;
    public ComboBox simBetSpreadCombo;
    public Spinner simRoundsPerHourSpinner;
    public Spinner simHoursSpinner;
    public Spinner simChipValue;
    public TextField simNumGamesToSim;
    public ComboBox simBettingUnitsCombo;
    public ComboBox bettingUnitsCombo;
    public TextField simDeckPenetration;
    public CheckBox wongingStyleCheckBox;

    public void initialise(){

        SimulationBankTracker test = new SimulationBankTracker(3200.0, 180);
        test.printInfo();

        //initialising playersCombo
        playersCombo.setItems(FXCollections.observableArrayList(1));
        //initialising decksCombo
        decksCombo.setItems(FXCollections.observableArrayList(1,2,3,4,5,6,7,8));
        //initialising CardCountingMethodsCombo
        CardCountingMethodsCombo.setItems(FXCollections.observableArrayList("Ace-Five Count", "High-Low Count", "Red-Seven", "Knock-Out", "Zen Count", "OmegaII"));
        //initialising betSpread combo
        betSpreadCombo.setItems(FXCollections.observableArrayList("1/8", "1/16", "1/32"));
        //initialising betUnits combo
        bettingUnitsCombo.setItems(FXCollections.observableArrayList(100, 200, 300, 400, 500, 600, 700, 800, 900, 1000));
        //initialising simulation area
        initialiseSimulation();
    }

    public void betSpreadComboAction(ActionEvent actionEvent) {
    }

    public void playButtonAction(ActionEvent actionEvent) throws IOException {
        int bankroll = Integer.parseInt(bankrollTextField.getText());
        //int minBet = Integer.parseInt(minBetTextField.getText());
        int playersNum = (int) playersCombo.getValue();
        int decksNum = (int) decksCombo.getValue();

        String[] betSpreadInput = betSpreadCombo.getValue().toString().split("/");
        int betSpread = Integer.parseInt(betSpreadInput[1]);

        int bettingUnits = Integer.parseInt(bettingUnitsCombo.getValue().toString());

        int minBet = Integer.parseInt(minBetTextField.getText());

        //building players list
        ArrayList<Player> players = new ArrayList<Player>();

        for(int i = 0; i < playersNum; i++){
            Player newPlayer = new Player(bankroll, CardCountingMethodsCombo.getValue().toString(), betSpread, decksNum, bettingUnits);
            players.add(newPlayer);
        }

        //System.out.println(players.toString());

        //building shoe
        Shoe shoe = new Shoe(decksNum);

        //loading fxml file with chosen parameters
        FXMLLoader loader = new FXMLLoader(getClass().getResource("BlackJackTable.fxml"));

        Parent popUpParent = loader.load();

        //controller method called to initialise combo box with equations list input
        BlackjackTable_Controller blackJackTableController = loader.getController();
        blackJackTableController.initialise(players, shoe, minBet, "blue");

        Scene popUpScene = new Scene(popUpParent);

        Stage stage = new Stage();
        stage.setScene(popUpScene);

        stage.showAndWait();

    }

    public void bettingUnitsComboAction(ActionEvent actionEvent) {
        int bettingUnitSelection = Integer.parseInt((bettingUnitsCombo.getValue().toString()));
        this.minBet = Integer.parseInt(bankrollTextField.getText()) / bettingUnitSelection;
        minBetTextField.setText(String.valueOf(minBet));
    }

    //////SIMULATION CODE//////

    public void initialiseSimulation(){

        //initialising playersCombo
        simPlayersCombo.setItems(FXCollections.observableArrayList(1));
        //initialising decksCombo
        simDecksCombo.setItems(FXCollections.observableArrayList(1,2,3,4,5,6,7,8));
        //initialising CardCountingMethodsCombo
        simCardCountingMethodsCombo.setItems(FXCollections.observableArrayList("None", "Ace-Five Count", "High-Low Count", "Red-Seven", "Knock-Out", "Zen Count", "OmegaII"));
        //initialising betSpread combo
        simBetSpreadCombo.setItems(FXCollections.observableArrayList("1/8", "1/16", "1/32"));
        //initialising simBettingUnitsCombo
        simBettingUnitsCombo.setItems(FXCollections.observableArrayList(100, 200, 300, 400, 500, 600, 700, 800, 900, 1000));

        //initialising Spinners
        SpinnerValueFactory<Integer> RPHFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(60, 120);
        RPHFactory.setValue(90);
        simRoundsPerHourSpinner.setValueFactory(RPHFactory);

        SpinnerValueFactory<Integer> HoursFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(2, 18);
        HoursFactory.setValue(4);
        simHoursSpinner.setValueFactory(HoursFactory);

        SpinnerValueFactory<Integer> ChipValue = new SpinnerValueFactory.IntegerSpinnerValueFactory(5, 100);
        ChipValue.setValue(5);
        simChipValue.setValueFactory(ChipValue);

    }

    public boolean checkSimulationFields(){

        if (Integer.parseInt(simNumGamesToSim.getText()) < 1000){
            Alert variableDefinedAlert = new Alert(Alert.AlertType.ERROR);
            variableDefinedAlert.setTitle("Incompatible Simulation Variables");
            variableDefinedAlert.setHeaderText("The information you have entered is incorrect");
            variableDefinedAlert.setContentText("The number of games to simulate cannot be less than 1000 \nas this will" +
                    " return a potentially misleading results due to a \nrelatively-small data set");

            variableDefinedAlert.showAndWait();
            return false;
        }

        if (simPlayersCombo.getValue() != null && simDecksCombo.getValue() != null && simCardCountingMethodsCombo.getValue() != null && simBetSpreadCombo != null) {
            if (simBankrollTextField.getText() != null && simMinBetTextField.getText() != null){
                return simRoundsPerHourSpinner.getValue() != null && simHoursSpinner.getValue() != null && simChipValue.getValue() != null;
            }
        }
        return false;
    }

    public void simulateButtonAction(ActionEvent actionEvent) throws IOException {
        if (checkSimulationFields()){
            buildSimulation();
        } else {
            Alert variableDefinedAlert = new Alert(Alert.AlertType.ERROR);
            variableDefinedAlert.setTitle("Incomplete Simulation Info");
            variableDefinedAlert.setHeaderText("The information you have entered is incorrect");
            variableDefinedAlert.setContentText("Please check you have filled out all simulation input boxes");

            variableDefinedAlert.showAndWait();
        }
    }

    public void simBetSpreadComboAction(ActionEvent actionEvent) {
    }


    private void buildSimulation() throws IOException {
        /*
        public ComboBox simPlayersCombo;
        public ComboBox simDecksCombo;
        public TextField simBankrollTextField;
        public TextField simMinBetTextField;
        public Button simulateButton;
        public ComboBox simCardCountingMethodsCombo;
        public ComboBox simBetSpreadCombo;
        public Spinner simRoundsPerHourSpinner;
        public Spinner simHoursSpinner;
        public Spinner simChipValue;
         */

        int playersNum = Integer.parseInt(simPlayersCombo.getValue().toString());
        int decksNum = Integer.parseInt(simDecksCombo.getValue().toString());
        int bankRoll = Integer.parseInt(simBankrollTextField.getText());
        int minBet = Integer.parseInt(simMinBetTextField.getText());
        String CCMethod = simCardCountingMethodsCombo.getValue().toString();
        int roundPH = Integer.parseInt(simRoundsPerHourSpinner.getValue().toString());
        int hoursNum = Integer.parseInt(simHoursSpinner.getValue().toString());
        int chipValue = Integer.parseInt(simChipValue.getValue().toString());
        int gamesToSim = Integer.parseInt(simNumGamesToSim.getText());
        int bettingUnits = Integer.parseInt(simBettingUnitsCombo.getValue().toString());

        //"wonging" is used to describe when a player only enters the table when the shoe is at a positive count of 2
        //or more, increasing the odds of winning consistently significantly at the cost of added staff-suspicion
        boolean wongingBool = wongingStyleCheckBox.isSelected();
        System.out.println(wongingBool);

        String[] betSpreadInput = simBetSpreadCombo.getValue().toString().split("/");
        int betSpread = Integer.parseInt(betSpreadInput[1]);

        int deckPenetration = Integer.parseInt(simDeckPenetration.getText());


        //building players list
        Player newPlayer = new Player(bankRoll, CCMethod, betSpread, decksNum, bettingUnits);

        //loading fxml file with chosen parameters
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Simulation.fxml"));

        Parent popUpParent = loader.load();

        //controller method called to initialise combo box with equations list input
        Simulation_Controller simulationController = loader.getController();
        simulationController.initialise(newPlayer, decksNum, bankRoll, betSpread, minBet, gamesToSim, roundPH, hoursNum, chipValue, bettingUnits, deckPenetration, wongingBool);

        Scene popUpScene = new Scene(popUpParent);

        Stage stage = new Stage();
        stage.setScene(popUpScene);


        stage.showAndWait();

    }

    public void simBettingUnitsComboAction(ActionEvent actionEvent) {
        int bettingUnitSelection = Integer.parseInt((simBettingUnitsCombo.getValue().toString()));
        this.minBet = Integer.parseInt(simBankrollTextField.getText()) / bettingUnitSelection;
        simMinBetTextField.setText(String.valueOf(minBet));
    }


}
