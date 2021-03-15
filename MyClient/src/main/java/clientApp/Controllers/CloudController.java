package clientApp.Controllers;

import clientApp.Client;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

// Основной класс по работе с окном Клиентского приложения
public class CloudController implements Initializable {
    private ObservableList<String> list;
    private Client client;
    private String listFilesOnServer;
    private String pcPath;
    private boolean sortFlag = true;


    public CloudController() {
        client = Client.getInstance();
        list = FXCollections.observableArrayList();
//        pcPath = "C:/Users/litva/IdeaProjects/MyCloudStorage/MyClient/src/main/resources/myDir";
        pcPath = "MyClient/src/main/resources/myDir";
    }

    // Создание новой директории
    public void mkdir() {
        if (!folderName.getText().isEmpty()) {
            String name = folderName.getText();
            client.sendMessage("mkdir " + name);
            if (client.readMessage().equals("dirSuccess")) {
                client.sendMessage("ls");
                listFilesOnServer = client.readMessage();
                updateListViewer(list, listFilesOnServer, cloudFilesList);
                folderName.setStyle("-fx-border-color: grey;");
                folderName.clear();
            }
        } else {
            folderName.setPromptText("Enter new name");
            folderName.setStyle("-fx-border-color: red;");
        }

    }

    //Удаление папки или файла на сервере
    public void remove(ActionEvent actionEvent) {
        if (!cloudFilesList.getSelectionModel().getSelectedItem().isEmpty()
                && !cloudFilesList.getSelectionModel().getSelectedItem().equals("<- Back")) {
            String name = cloudFilesList.getSelectionModel().getSelectedItem();
            client.sendMessage("rm " + name);
            if (client.readMessage().equals("rmSuccess")) {
                client.sendMessage("ls");
                listFilesOnServer = client.readMessage();
                updateListViewer(list, listFilesOnServer, cloudFilesList);
            }
        }

    }

    //Сортировка списка файлов в списке файлов сервера (ListView). Криво выглядит, но работает. К ней вернусь еще.
    public void sortListView(ActionEvent actionEvent) {
        client.sendMessage("ls");
        listFilesOnServer = client.readMessage();
        updateListViewer(list,listFilesOnServer,cloudFilesList);
        cloudFilesList.getItems().stream().sorted();
        ArrayList<String> sb1 = new ArrayList<>();
        ArrayList<String> sb2 = new ArrayList<>();
        String tmp;
        ObservableList<String> str = cloudFilesList.getItems();
        for (String s : str) {
            if (s.contains(".")) {
                sb1.add(s);
            } else if (!s.contains("<- Back")) {
                sb2.add(s);
            }
        }
        if (sortFlag) {
            tmp = (sb1.toString() + " " + sb2.toString());
            sortFlag = !sortFlag;
        } else {
            tmp = (sb2.toString() + " " + sb1.toString());
            sortFlag = !sortFlag;

        }
        tmp = tmp.replace(",", "")
                .replace("[", "")
                .replace("]", "");
        System.out.println(tmp);
        updateListViewer(list, tmp, cloudFilesList);

    }

    //Получение списка файлов на ПК пользователя. Пока папка конкретная...
    public String getPcFilesList(String dir) {
        File file = new File(dir);
        File[] files = file.listFiles();
        StringBuffer sb = new StringBuffer();
        for (File f : files) {
            sb.append(f.getName()).append(" ");
        }
        return sb.toString();
    }

    // Обновление списка файлов сервера в пользовательском приложении.
    public void updateListViewer(ObservableList list, String listFilesOnServer, ListView listView) {
        listView.getItems().clear();
        list.removeAll(list);
        String[] files = listFilesOnServer.trim().split(" ");
        list.addAll("<- Back");
        list.addAll(files);
        listView.getItems().addAll(list);
        addressLine.setText(getAddressLine());
    }

    //Выбор элемента по двойному клику
    public void selectItem(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 2 && !cloudFilesList.getSelectionModel().getSelectedItems().isEmpty()) {
            String name = cloudFilesList.getSelectionModel().getSelectedItem();
            cd(name);
        }
    }

    //Смена директории
    public void cd(String dir) {
        if (dir.equals("<- Back")) dir = "back";
        client.sendMessage("cd " + dir);
        System.out.println(client.readMessage());
        client.sendMessage("ls");
        listFilesOnServer = client.readMessage();
        updateListViewer(list, listFilesOnServer, cloudFilesList);
    }

    //Получение адреса папки на сервере
    public String getAddressLine() {
        client.sendMessage("getAddress");
        return client.readMessage();
    }

    //cd на ПК по папкам
    public void cdOnPc(ActionEvent actionEvent) {
        if (!addressPC.getText().isEmpty())
            pcPath = addressPC.getText();
        updateListViewer(list, getPcFilesList(pcPath), pcFilesList);
    }

    // Инит на старте пргораммы
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        client.sendMessage("ls");
        listFilesOnServer = client.readMessage();
        updateListViewer(list, listFilesOnServer, cloudFilesList);
        updateListViewer(list, getPcFilesList(pcPath), pcFilesList);
        addressPC.setText(pcPath);
    }

    @FXML
    private Button go;

    @FXML
    TextField addressPC;

    @FXML
    private TextField folderName;

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

    @FXML
    private TextField addressLine;


    public void exit(ActionEvent actionEvent) {
        Platform.exit();
    }


}
