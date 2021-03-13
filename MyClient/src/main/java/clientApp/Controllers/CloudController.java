package clientApp.Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;

public class CloudController {

    @FXML
    private Button searchButton;

    @FXML
    private MenuBar menuBar;

    @FXML
    private ListView<?> cloudFilesList;

    @FXML
    private ListView<?> pcFilesList;

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
