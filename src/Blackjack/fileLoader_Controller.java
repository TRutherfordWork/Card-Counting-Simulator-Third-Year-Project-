package Blackjack;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class fileLoader_Controller {

    public TableView saveDataTable;
    public TableColumn fileNameCol;
    public Simulation_Controller parentObejct;
    public Visualisation_Controller parentObjectOverlay;
    private boolean Overlay;

    public void initialise(Simulation_Controller parentObject, File[] saveFiles){

        this.parentObejct = parentObject;
        this.Overlay = false;

        constructFileList(saveFiles);

    }

    public void initialise(Visualisation_Controller parentObject, File[] saveFiles){

        this.parentObjectOverlay = parentObject;
        this.Overlay = true;

        constructFileList(saveFiles);

    }

    private void constructFileList(File[] saveFiles){
        List<String> fileNames = new ArrayList<>();

        for(File file :  saveFiles){
            fileNames.add(file.getName());
        }

        ObservableList<fileConstructor> fileTableInfo = FXCollections.observableArrayList();;

        for(int i = 0; i < fileNames.size(); i++){
            fileConstructor fileConstruct = new fileConstructor(fileNames.get(i));
            fileTableInfo.add(fileConstruct);
        }

        constructTable(fileTableInfo);
    }

    private void constructTable(ObservableList<fileConstructor> fileTableInfo){

        fileNameCol.setCellValueFactory(new PropertyValueFactory<Simulation_Controller, String>("fileName"));

        saveDataTable.setItems(fileTableInfo);

    }

    public void mouseClickedEvent(MouseEvent mouseEvent) throws IOException {

        if (mouseEvent.getClickCount() == 2) {
            fileConstructor selectedFile = (fileConstructor) saveDataTable.getSelectionModel().getSelectedItem();
            loader(selectedFile);
        }

    }

    private void loader(fileConstructor selectedFile) throws IOException {

        //closing window
        Stage stage = (Stage) saveDataTable.getScene().getWindow();
        stage.close();

        if(!Overlay) {
            parentObejct.loadData(selectedFile.getFileName());
        }
        else{
            parentObjectOverlay.overlay(selectedFile.getFileName());
        }
    }
}
