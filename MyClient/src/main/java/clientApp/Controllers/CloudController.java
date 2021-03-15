package clientApp.Controllers;

import clientApp.Client;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class CloudController implements Initializable {
    ObservableList<String> list;
    Client client;
    String listFilesOnServer;
    String pcPath;

    public CloudController() {
        client = Client.getInstance();
        list = FXCollections.observableArrayList();
        pcPath = "MyClient/src/main/resources/myDir";
    }

    public void createNewDir() {
        client.sendMessage("createDir");
        if(client.readMessage().equals("dirSuccess")){
            client.sendMessage("list-files");
            listFilesOnServer = client.readMessage();
            updateListViewer(list, listFilesOnServer, cloudFilesList);
        }
    }

    public String getPcFilesList(String dir) {
        File file = new File(dir);
        File[] files = file.listFiles();
        StringBuffer sb = new StringBuffer();
        for (File f : files) {
            sb.append(f.getName()).append(" ");
        }
        return sb.toString();

    }


    public void updateListViewer(ObservableList list, String listFilesOnServer, ListView listView) {
        listView.getItems().clear();
        list.removeAll(list);
        String[] files = listFilesOnServer.trim().split(" ");
        list.addAll("<- Back");
        list.addAll(files);
        listView.getItems().addAll(list);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        client.sendMessage("list-files");
        listFilesOnServer = client.readMessage();
        updateListViewer(list, listFilesOnServer, cloudFilesList);
        updateListViewer(list, getPcFilesList(pcPath), pcFilesList);
    }


    @FXML
    private Button searchButton;

    @FXML
    private MenuBar menuBar;

    @FXML
    private ListView<String> cloudFilesList;

    @FXML
    private ListView<String> pcFilesList;

    @FXML
    private Button copyButton;

    @FXML
    private Button cutButton;

    @FXML
    private Button pasteButton;

    @FXML
    private Button dirButton;

    @FXML
    private Button sortButton;

    @FXML
    private Button removeButton;

    @FXML
    private Button downloadButton;

    @FXML
    private Button uploadButton;

    @FXML
    private TextField searchLabel;

    @FXML
    private TextField freeSpace;

    @FXML
    private ProgressBar progressBar;


}
