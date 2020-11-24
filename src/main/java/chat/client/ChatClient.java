package chat.client;

import chat.message.*;
import chat.transport.MessageSocket;
import event.EventBus;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableStringValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.Socket;

enum InternalEvent {
    NonUniqueUsernameError,
    NeedsReident
}


public class ChatClient extends Application {
    private final ObservableList<Message> chatEntries = FXCollections.observableArrayList();
    private final EventBus<InternalEvent> ev = new EventBus<>();
    private final BooleanProperty connected = new SimpleBooleanProperty(false);
    private boolean dialogOpen = false;
    MessageSocket<IChatApiMessage> socket;

    public static void main(String[] args) {
        launch(args);
    }

    private void runSocket() {
        var thread = new Thread(() -> {
            while (true) {
                try {
                    var rawSocket = new Socket("localhost", 2048);
                    socket = new MessageSocket<>(rawSocket);
                    System.out.println("--- Connected to server: " + rawSocket.getRemoteSocketAddress());
                    connected.set(true);
                    while (true) {
                        IChatApiMessage msg = socket.read();
                        if (msg == null) {
                            System.out.println("--- Got null from socket, exiting");
                            return;
                        }
                        if (msg instanceof NonUniqueUsernameError) {
                            ev.emit(InternalEvent.NonUniqueUsernameError);
                        } else if (msg instanceof NewMessageEvent) {
                            Message message = ((NewMessageEvent) msg).message;
                            Platform.runLater(() -> chatEntries.add(message));

                            System.out.println("--- Got message: " + message.content + " from " + message.author);
                        }
                    }
                } catch (IOException | ClassNotFoundException e) {
                    connected.set(false);
                    ev.emit(InternalEvent.NeedsReident);
                    try {
                        System.out.println("=== Unable to connect to server, waiting before retrying!");
                        Thread.sleep(2000);
                    } catch (InterruptedException interruptedException) {
                        interruptedException.printStackTrace();
                    }
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    private String formatIdentityLabel(String username) {
        if (username == null) {
            return "Not logged in";
        } else {
            return "Logged in as: " + username;
        }
    }

    private HBox buildStatusBar(ObservableStringValue username) {
        var identityLabel = new Label();
        identityLabel.setText(formatIdentityLabel(username.get()));
        username.addListener((obs, old, newVal) -> Platform.runLater(() -> identityLabel.setText(formatIdentityLabel(newVal))));
        var connectionlabel = new Label();
        connectionlabel.setText(connected.get() ? "Connected" : "Disconnected");
        connected.addListener((obs, old, newVal) -> Platform.runLater(() -> connectionlabel.setText(newVal ? "Connected" : "Disconnected")));

        HBox hBox = new HBox(connectionlabel, identityLabel);
        hBox.setSpacing(5);
        return hBox;
    }

    @Override
    public void start(Stage stage) {
        var username = new SimpleStringProperty(getParameters().getNamed().get("username"));

        runSocket();

        var chatText = new ListView<>(chatEntries);
        chatText.getItems().addListener((ListChangeListener<Message>) change ->
                chatText.scrollTo(change.getList().size() - 1));

        chatText.setCellFactory((listView) -> {
            var cell = new ListCell<Message>();

            cell.itemProperty().addListener((obs, old, item) -> {
                if (item != null) {
                    cell.setText(item.author + ": " + item.content);
                } else {
                    cell.setText("");
                }
            });

            return cell;
        });

        var textEntryField = new TextField();
        textEntryField.setPromptText("Type a message");
        textEntryField.setOnAction(e -> {
            try {
                if (socket == null) return;
                socket.write(new MessageCmd(textEntryField.getText()));
            } catch (IOException ioException) {
                ioException.printStackTrace();
                return;
            }

            textEntryField.setText("");
        });

        var root = new BorderPane();
        root.setTop(buildStatusBar(username));
        root.setCenter(chatText);
        root.setBottom(textEntryField);
        root.autosize();

        root.setOnKeyPressed(e -> textEntryField.requestFocus());


        var scene = new Scene(root);
        scene.getStylesheets().add("style.css");
        stage.setTitle("Chat");
        stage.setScene(scene);
        stage.show();

        ev.subscribe((event) -> {
            if (event.equals(InternalEvent.NonUniqueUsernameError)) {
                username.set(null);
                Platform.runLater(() -> showUsernamePromptDialog(stage, username, true));
            } else if (event.equals(InternalEvent.NeedsReident)) {
                Platform.runLater(() -> showUsernamePromptDialog(stage, username, false));
            }
        });

        if (username.get() == null) {
            showUsernamePromptDialog(stage, username, false);
        } else {
            try {
                if (socket != null) {
                    socket.write(new IdentifyRequest(username.get()));
                } else {
                    username.set(null);
                    showUsernamePromptDialog(stage, username, false);
                }
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    private void showUsernamePromptDialog(Stage root, SimpleStringProperty username, boolean error) {
        if (dialogOpen) {
            return;
        } else {
            dialogOpen = true;
        }
        var dialog = new Stage();

        var usernameField = new TextField();
        Label label;
        if (error) {
            label = new Label("Enter a different username:");
            label.setTextFill(Color.RED);
        } else {
            label = new Label("Enter a username:");
            label.setTextFill(Color.BLACK);
        }
        var login = new HBox(label, usernameField);
        usernameField.setOnAction(e -> {
            try {
                socket.write(new IdentifyRequest(usernameField.getText()));
                username.set(usernameField.getText());
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            dialog.close();
        });

        dialog.setScene(new Scene(login));

        dialog.initOwner(root);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setOnCloseRequest(e -> {
            if (usernameField.getText().isEmpty()) {
                root.close();
            }
        });
        dialog.showAndWait();
    }
}