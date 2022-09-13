package Blackjack;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;

public class Visualisation_Controller {
    public ScatterChart changeInWinScatterChart;
    public BarChart countFrequencyBarChart;
    public StackedBarChart WLDStacked;
    public LineChart playerHouseEdgeChart;
    private AreaChart<?,?> areaChart;

    public void initialise(List<SimulationStatCollector> stats){

        //removing legend from single element charts as it is not necessary
        changeInWinScatterChart.setLegendVisible(true);
        countFrequencyBarChart.setLegendVisible(true);

        //instantiating the new XYChartSeries objects to contain the data which is to be displayed
        XYChart.Series tableDataFrequency = new XYChart.Series();
        XYChart.Series tableDataChangeInWinRate = new XYChart.Series();

        tableDataFrequency.setName("Current Method");
        tableDataChangeInWinRate.setName("Current Method");


        //initialising ChangeInWinScatterChart & CountFrequencyBarChart

        //converting List of objects into List of HashMaps of selected data
        List<HashMap<String, Double>> selectedData = extractData(stats);

            //specific series allocated for WLDBarChart
        final XYChart.Series<String, Double> winSeries = new XYChart.Series<>();
        final XYChart.Series<String, Double> drawSeries = new XYChart.Series<>();
        final XYChart.Series<String, Double> lossSeries = new XYChart.Series<>();
        final XYChart.Series<String, Double> edgeSeries = new XYChart.Series<>();

        winSeries.setName("Wins(%)");
        drawSeries.setName("Draws(%)");
        lossSeries.setName("Losses(%)");


        //adding selected data to XYChartSeries
        Double value;
        Double win;
        Double loss;
        Double edge;
        List<String> keys = Arrays.asList("<=-1", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", ">=19");
        for(String key : keys){
                    //count frequency data
            value = selectedData.get(0).get(key);
            //error handling of NaN results
            if(Double.isNaN(value)){value = 0.0;}
            System.out.println(value);
            tableDataFrequency.getData().add(new XYChart.Data(key, value));

                   //changeInWin data
            value = selectedData.get(1).get(key);
            //error handling of NaN results
            if(!(Double.isNaN(value))) {
                System.out.println(value);
                tableDataChangeInWinRate.getData().add(new XYChart.Data(key, value));
            }

            //creating stacked bar chart for visualising win/loss/draw

                //Win Data
            value = selectedData.get(2).get(key);
            if(Double.isNaN(value)){value = 0.0;}
            win=value;
            winSeries.getData().add(new XYChart.Data<>(key, value));
                //Draw Data
            value = selectedData.get(4).get(key);
            if(Double.isNaN(value)){value = 0.0;}
            drawSeries.getData().add(new XYChart.Data<>(key, value));
                //Loss Data
            value = selectedData.get(3).get(key);
            if(Double.isNaN(value)){value = 0.0;}
            loss=value;
            lossSeries.getData().add(new XYChart.Data<>(key, value));

            if(win!=0 && loss!=0) {
                edge = win - loss;
                edgeSeries.getData().add(new XYChart.Data<>(key, edge));
            }
        }

        //associating chart with data for data-display
        countFrequencyBarChart.getData().add(tableDataFrequency);
        changeInWinScatterChart.getData().add(tableDataChangeInWinRate);
        WLDStacked.getData().addAll(lossSeries, drawSeries, winSeries);
        playerHouseEdgeChart.getData().add(edgeSeries);

        //buildEdgeChart(selectedData);

    }

    private List<HashMap<String, Double>> extractData(List<SimulationStatCollector> stats){
        //Constructing List of hashMaps to place key-value pairs equal to Count<String> : Data<Double>
        List<HashMap<String, Double>> mappedData = new ArrayList<>();
        for(int i = 0; i < 5; i++) {
            mappedData.add(new HashMap<String, Double>());
        }
        String key;
        double value;

        for(SimulationStatCollector object : stats){
            key = object.getCount();
            value = Double.parseDouble(object.getFrequency());
            //inserting frequency data into hashMap at index 0
            mappedData.get(0).put(key, value);
            //inserting ChangeInWinPercentage into hashMap at index 1
            value = Double.parseDouble(object.getChangeInWinPercentage());
            mappedData.get(1).put(key, value);

            //inserting winPercentage into hashMap at index 2
            value = Double.parseDouble(object.getWin());
            //converting to percentage
            Double frequency = Double.parseDouble(object.getFrequency());
            value = (value / frequency) * 100;
            mappedData.get(2).put(key, value);

            //inserting lossPercentage into hashMap at index 3
            value = (Double.parseDouble(object.getLoss()) / frequency) * 100;
            mappedData.get(3).put(key, value);

            //inserting drawPercentage into hashMap at index 4
            value = (Double.parseDouble(object.getDraw()) / frequency) * 100;
            mappedData.get(4).put(key, value);


        }

        return mappedData;

    }

    //handles events where the user selects a file to overlay data

    public void compareButtonAction(ActionEvent actionEvent) throws IOException {

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


    public void overlay(String fileName) throws FileNotFoundException {

        String data = "";

        Scanner scanner = new Scanner(new File("SaveData/" + fileName));
        while (scanner.hasNext()) {
            data = data + scanner.next();
        }

        scanner.close();
        System.out.println(data);

        String[] splitList = data.split("]");
        List<List<String>> dataList = new ArrayList<>();

        for (String list : splitList) {

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

        for (List<String> list : dataList) {
            temp = new ArrayList<>();
            for (String attribute : list) {
                if (attribute != "") {
                    temp.add(attribute);
                }
            }
            newList.add(temp);
        }

        System.out.println(newList);

        //pushing SaveData into the tableView
        ObservableList<SimulationStatCollector> statList = FXCollections.observableArrayList();

        for (List<String> stats : newList) {
            //calculating changeInWinPercentage (simulation returned standard win rate being 42.9% )
            double change = Double.parseDouble(stats.get(5)) - 42.9;
            String changeInWinPercentage = new DecimalFormat("#.###").format(change);
            //reconstructing the SimulationStatCollector objects to be displayed from the SaveData
            statList.add(new SimulationStatCollector(stats.get(0), (float) Double.parseDouble(stats.get(1)), Double.parseDouble(stats.get(2)),
                    Double.parseDouble(stats.get(3)), Double.parseDouble(stats.get(4)), stats.get(5), stats.get(6), changeInWinPercentage));
        }

        //extracting data

        List<HashMap<String, Double>> extractedData = extractData(statList);

        System.out.println(extractedData.toString());

        //instantiating new XYSeries object for compatibility when overlaying data

        XYChart.Series tableDataChangeInWinRate = new XYChart.Series();
        XYChart.Series tableDataFrequency = new XYChart.Series();

        final XYChart.Series<String, Double> edgeSeries = new XYChart.Series<>();

        //setting names for legend
        tableDataFrequency.setName(fileName.replace(".JSON", ""));
        tableDataChangeInWinRate.setName(fileName.replace(".JSON", ""));

        edgeSeries.setName(fileName.replace(".JSON", ""));

        //adding selected data to XYChartSeries
        Double value;
        Double win;
        Double loss;
        Double edge;
        List<String> keys = Arrays.asList("<=-1", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", ">=19");
        for (String key : keys) {
            //count frequency data
            value = extractedData.get(0).get(key);
            //error handling of NaN results
            if(Double.isNaN(value)){value = 0.0;}
            System.out.println(value);
            tableDataFrequency.getData().add(new XYChart.Data(key, value));

            //changeInWin data
            value = extractedData.get(1).get(key);
            //error handling of NaN results
            if(!(Double.isNaN(value))) {
                System.out.println(value);
                tableDataChangeInWinRate.getData().add(new XYChart.Data(key, value));
            }

            //Win Data
            value = extractedData.get(2).get(key);
            if(Double.isNaN(value)){value = 0.0;}
            win=value;
            //Loss Data
            value = extractedData.get(3).get(key);
            if(Double.isNaN(value)){value = 0.0;}
            loss=value;

            if(win!=0 && loss !=0) {
                edge = win - loss;
                edgeSeries.getData().add(new XYChart.Data<>(key, edge));
            }
        }

        changeInWinScatterChart.getData().add(tableDataChangeInWinRate);
        countFrequencyBarChart.getData().add(tableDataFrequency);
        playerHouseEdgeChart.getData().add(edgeSeries);

    }

}
