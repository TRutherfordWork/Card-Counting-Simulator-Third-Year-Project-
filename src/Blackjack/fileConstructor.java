package Blackjack;

public class fileConstructor {

    //whilst only holding one attribute, this class is necessary for the compatibility of the fileName table in
    // fileLoader.fxml

    private String fileName;

    fileConstructor(String fileName){
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

}
