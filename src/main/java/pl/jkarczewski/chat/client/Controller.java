package pl.jkarczewski.chat.client;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Class responsible for UI functionality.
 */
public class Controller implements Initializable {

    @FXML private TextField serverAddressField;
    @FXML private TextField serverPortField;
    @FXML private TextField nicknameField;

    @FXML private TextArea messageArea;

    @FXML private ListView chatLog;

    @FXML private Button disconnectButton;
    @FXML private Button connectButton;
    @FXML private Button sendButton;

    private ObservableList<String> chatMessages;

    private ChatClient chatClient;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        chatMessages = FXCollections.observableArrayList("SYSTEM: Welcome to Chatter :)");
        chatLog.setItems(chatMessages);

        setConnected(false);

        connectButton.setOnAction(event -> {
            String serverHost = serverAddressField.getText();
            String serverPortString = serverPortField.getText();
            String nickname = "anonymous";

            if (serverHost.length() == 0 || serverPortString.length() == 0) {
                addMessage("SYSTEM: Bad connection configuration!");
                return;
            }

            int serverPort;
            try {
                serverPort = Integer.parseInt(serverPortString);
            } catch (NumberFormatException ex) {
                addMessage("SYSTEM: Bad number format for server port!");
                return;
            }

            if (nicknameField.getText().length() != 0) {
                if (nicknameField.getText().split(" ").length != 1) {
                    addMessage("SYSTEM: Nickname cannot contain spaces");
                }

                nickname = nicknameField.getText().trim();
            } else {
                nicknameField.setText(nickname);
            }

            chatClient = new ChatClient(this);
            chatClient.connect(serverHost, serverPort);

            // trigger joined to server event
            chatClient.send("!JOINED " + nickname);
        });

        sendButton.setOnAction(event -> {
            String msg = messageArea.getText();
            messageArea.setText("");

            chatClient.send(msg);
        });

        disconnectButton.setOnAction(event -> chatClient.shut());

    }

    /**
     * Method used to add entry to chat log.
     *
     * @param msg String message, which should be added to chat log.
     */
    public void addMessage(String msg) {
        Platform.runLater(() -> chatMessages.add(msg));
    }

    /**
     * Method responsible for controlling Disabled/Enabled state of UI components.
     *
     * @param connected boolean: true -> client connected to server; false  -> client disconnected
     */
    public void setConnected(boolean connected) {

        serverAddressField.setDisable(connected);
        serverPortField.setDisable(connected);
        nicknameField.setDisable(connected);

        messageArea.setDisable(!connected);

        disconnectButton.setDisable(!connected);
        connectButton.setDisable(connected);
        sendButton.setDisable(!connected);
    }
}
