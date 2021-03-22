package clientApp.Controllers;

import clientApp.Client;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

import java.awt.*;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
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
        pcPath = "MyClient/src/main/resources/myDir";
//        pcPath = "C:/";
    }

    // Создание новой директории
    public void mkdir() {
        String name = folderName.getText().trim().replace(" ", "??");

        if (!name.isEmpty()) {
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
            client.sendMessage("rm " + name.replace(" ", "??"));
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
        updateListViewer(list, listFilesOnServer, cloudFilesList);
        cloudFilesList.getItems().stream().sorted();
        ArrayList<String> sb1 = new ArrayList<>();
        ArrayList<String> sb2 = new ArrayList<>();
        String tmp;
        ObservableList<String> str = cloudFilesList.getItems();

        for (int i = 0; i < str.size(); i++) {
            String tmpstr = str.get(i).replace(" ", "??");
            if (tmpstr.contains(".")) {
                sb1.add(tmpstr);
            } else if (!tmpstr.contains("<-??Back")) {
                sb2.add(tmpstr);
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

    //Получение списка файлов на ПК пользователя.
    public String getPcFilesList(String dir) {
        File file = new File(dir);
        File[] files = file.listFiles();
        StringBuffer sb = new StringBuffer();
        if(files == null) return sb.toString();
        for (File f : files) {
            sb.append(f.getName().replace(" ", "??")).append(" ");
        }
        return sb.toString();
    }

    // Обновление списка файлов сервера в пользовательском приложении.
    public void updateListViewer(ObservableList list, String listFilesOnServer, ListView listView) {
        listView.getItems().clear();
        list.removeAll(list);
        String[] files = listFilesOnServer.trim().split(" ");
        for (int i = 0; i < files.length; i++) {
            files[i] = files[i].replace("??", " ");
        }
        list.addAll("<- Back");
        if(Arrays.asList(files).get(0).isEmpty()){
            list.addAll("Empty");
        } else {
            list.addAll(files);
        }
        listView.getItems().addAll(list);
        addressLine.setText(getAddressLine());
    }

    //Выбор элемента по двойному клику Cloud
    public void selectItem(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 2 && !cloudFilesList.getSelectionModel().getSelectedItems().isEmpty()) {
            String name = cloudFilesList.getSelectionModel().getSelectedItem()
                    .replace(" ", "??")
                    ;
            System.out.println(name);
            cd(name);
        }
    }

    //Выбор элемента по двойному клику PC
    public void selectItemPC(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 2 && !pcFilesList.getSelectionModel().getSelectedItems().isEmpty() ) {
            String name = pcFilesList.getSelectionModel().getSelectedItem();
            System.out.println(name);
            if(name.equals("<- Back")){
                pcPath = getPreviousPath(pcPath);
            } else if (new File(pcPath + "/" + name).isDirectory()){
                pcPath = pcPath + "/" + name;
            }
            updateListViewer(list,getPcFilesList(pcPath),pcFilesList);
            File file = new File(pcPath);
            pcPath = file.getAbsolutePath().replaceAll("\\\\", "/");
            addressPC.setText(pcPath);
        }
    }
// Получение строки с адресом для Back
    public String getPreviousPath(String path) {

        int index = -1;
        for (int i = 0; i < path.length(); i++) {
            if (path.charAt(i) == '/') {
                index = i;
            }
        }
        path = path.substring(0, index);
        System.out.println(path);
        if (path.equals("C:")) path = "C:/";
        return path;
    }

    //Смена директории
    public void cd(String dir) {
        if (dir.equals("<-??Back")) dir = "back";
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
        String tmp = addressPC.getText().trim();
        if (!addressPC.getText().trim().isEmpty() && new File(tmp).exists() && new File(tmp).listFiles() != null){
            pcPath = addressPC.getText();
            updateListViewer(list, getPcFilesList(pcPath), pcFilesList);
        } else {
            addressPC.clear();
            addressPC.setText(pcPath);
        }
    }

    //Копирование файла
    public void copyFile(ActionEvent actionEvent) {
        if (!cloudFilesList.getSelectionModel().getSelectedItem().isEmpty()
                && !cloudFilesList.getSelectionModel().getSelectedItem().equals("<- Back")) {
            String name = cloudFilesList.getSelectionModel().getSelectedItem().replace(" ", "??");
            client.sendMessage("copy " + name);
            client.readMessage();
        }
    }

    // Вырезание файла
    public void cut(ActionEvent actionEvent) {
        if (!cloudFilesList.getSelectionModel().getSelectedItem().isEmpty()
                && !cloudFilesList.getSelectionModel().getSelectedItem().equals("<- Back")) {
            String name = cloudFilesList.getSelectionModel().getSelectedItem().replace(" ", "??");
            client.sendMessage("cut " + name);
            client.readMessage();
        }
    }

    // Вставка файла
    public void paste(ActionEvent actionEvent) {
        client.sendMessage("paste");
        client.readMessage();
        client.sendMessage("ls");
        listFilesOnServer = client.readMessage();
        updateListViewer(list, listFilesOnServer, cloudFilesList);
    }

    // For fun
    public void openWebpage(ActionEvent actionEvent) {
        try {
            Desktop.getDesktop().browse(new URL("http://i.mycdn.me/i?r=AzEPZsRbOZEKgBhR0XGMT1RkUQz0tb6GH3YzGNzdL8pyWaaKTM5SRkZCeTgDn6uOyic").toURI());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // поиск файлов на сервере
    public void search(ActionEvent actionEvent) {
        if(searchLabel.getText().trim().isEmpty()) return;
        String searchStr = searchLabel.getText().trim();
        client.sendMessage("search " + searchStr);
        searchStr = client.readMessage();
        searchLabel.clear();
        if(searchStr.equals("Not Found")){
            searchLabel.setPromptText(searchStr);
            return;
        }
        searchLabel.setPromptText("Search file");
        updateListViewer(list, searchStr, cloudFilesList);
    }

    // Инит на старте программы
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
