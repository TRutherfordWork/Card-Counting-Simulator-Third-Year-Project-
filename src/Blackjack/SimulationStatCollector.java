package Blackjack;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SimulationStatCollector {


    //setting variables which will be accessed in order to display data in the tableView in simulation_Controller
    public String count;
    public String frequency;
    public String win;
    public String loss;
    public String draw;
    public String WLPercentage;
    public String LPercentage;
    public String changeInWinPercentage;

    //constructor
    SimulationStatCollector(String count, Float frequency, Double win, Double loss, Double draw, String WLPercentage, String LPercentage, String changeInWinPercentage){

        this.count = count;
        this.frequency = frequency.toString();
        this.win = win.toString();
        this.loss = loss.toString();
        this.draw = draw.toString();
        this.WLPercentage = WLPercentage;
        this.LPercentage = LPercentage;
        this.changeInWinPercentage = changeInWinPercentage;


    }

    public List<String> getAllData(){
        //class method used for the saving and loading of data to and from JSON (using com.google.GSON)

        List<String> dataList = new ArrayList<>();
        dataList.add(getCount());
        dataList.add(getFrequency());
        dataList.add(getWin());
        dataList.add(getLoss());
        dataList.add(getDraw());
        dataList.add(getWLPercentage());
        dataList.add(getLPercentage());

        return dataList;
    }

    public String getCount() {
        return count;
    }

    public String getFrequency() {
        return frequency;
    }

    public String getWin() {
        return win;
    }

    public String getLoss() {
        return loss;
    }

    public String getDraw() {
        return draw;
    }

    public String getWLPercentage() {
        return WLPercentage;
    }

    public String getLPercentage(){ return LPercentage; }

    public String getChangeInWinPercentage() {
        return changeInWinPercentage;
    }
}
