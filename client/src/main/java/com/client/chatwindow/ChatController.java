package com.client.chatwindow;

import com.client.login.MainLauncher;
import com.client.util.VoicePlayback;
import com.client.util.VoiceRecorder;
import com.client.util.VoiceUtil;
import com.messages.Bubble.BubbleSpec;
import com.messages.Bubble.BubbledLabel;
import com.messages.Message;
import com.messages.MessageType;
import com.messages.Status;
import com.messages.User;
import com.security.Crypro;
import com.traynotifications.animations.AnimationType;
import com.traynotifications.notification.TrayNotification;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;


public class ChatController implements Initializable {

    @FXML    private TextArea messageBox;
    @FXML    private Label usernameLabel;
    @FXML    private Label roomnameLabel;
    @FXML    private Label onlineCountLabel;
    @FXML    private ListView userList;
    @FXML    private ImageView userImageView;
    @FXML    private ListView chatPane;
    @FXML    private BorderPane borderPane;
    @FXML    private ComboBox statusComboBox;
    @FXML    private ImageView microphoneImageView;

    private Image microphoneActiveImage = new Image(getClass().getClassLoader().getResource("images/microphone-active.png").toString());
    private Image microphoneInactiveImage = new Image(getClass().getClassLoader().getResource("images/microphone.png").toString());

    private double xOffset;
    private double yOffset;
    private Logger logger = LoggerFactory.getLogger(ChatController.class);
    private boolean logout = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        borderPane.setOnMousePressed(event -> {
            xOffset = MainLauncher.getPrimaryStage().getX() - event.getScreenX();
            yOffset = MainLauncher.getPrimaryStage().getY() - event.getScreenY();
            borderPane.setCursor(Cursor.CLOSED_HAND);
        });

        borderPane.setOnMouseDragged(event -> {
            MainLauncher.getPrimaryStage().setX(event.getScreenX() + xOffset);
            MainLauncher.getPrimaryStage().setY(event.getScreenY() + yOffset);

        });

        borderPane.setOnMouseReleased(event -> {
            borderPane.setCursor(Cursor.DEFAULT);
        });

        statusComboBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                try {
                    Listener.sendStatusUpdate(Status.valueOf(newValue.toUpperCase()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        /* Added to prevent the enter from adding a new line to inputMessageBox */
        messageBox.addEventFilter(KeyEvent.KEY_PRESSED, ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                try {
                    sendButtonAction();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ke.consume();
            }
        });
    }

    private String formMessage(String msg, int leng) {
        StringBuilder stringBuilder = new StringBuilder(msg);
        if (msg.length() > leng) {
            int block = stringBuilder.length() / leng;
            for (int i = 1; i <= block; i++) {
                stringBuilder.insert(leng * i, (char) 10);
            }
        }
        return stringBuilder.toString();
    }

    public void sendMethod(KeyEvent event) throws IOException {
        if (event.getCode() == KeyCode.ENTER) {
            sendButtonAction();
        }
    }

    public void sendButtonAction() throws IOException {
        String msg = messageBox.getText();
        if (!messageBox.getText().isEmpty()) {
            printYourMessage(msg, MessageType.TEXT);
            Listener.send(msg);
            messageBox.clear();

        }
    }

    public void recordVoiceMessage() throws IOException {
        if (VoiceUtil.isRecording()) {
            Platform.runLater(() -> {
                        microphoneImageView.setImage(microphoneInactiveImage);
                    }
            );
            VoiceUtil.setRecording(false);
            printYourMessage("Record", MessageType.VOICE);
        } else {
            Platform.runLater(() -> {
                        microphoneImageView.setImage(microphoneActiveImage);
                    }
            );
            VoiceRecorder.captureAudio();

        }

    }

    void newUserNotification(Message msg) {
        Platform.runLater(() -> {
            Image profileImg = new Image(getClass().getClassLoader().getResource("images/" + msg.getPicture() + ".png").toString(), 50, 50, false, false);
            TrayNotification tray = new TrayNotification();
            tray.setTitle("A new user has joined!");
            tray.setMessage(msg.getName() + " has joined the Chatroom!");
            tray.setRectangleFill(Paint.valueOf("#2C3E50"));
            tray.setAnimationType(AnimationType.POPUP);
            tray.setImage(profileImg);
            tray.showAndDismiss(Duration.seconds(1));
            try {
                Media hit = new Media(getClass().getClassLoader().getResource("sounds/notification.wav").toString());
                MediaPlayer mediaPlayer = new MediaPlayer(hit);
                mediaPlayer.play();
            } catch (Exception e) {
                e.printStackTrace();
            }

        });
    }

    synchronized void printOtherMessage(Message msg) {
        Task<HBox> othersMessages = new Task<HBox>() {
            @Override
            public HBox call() throws Exception {
                Image image = new Image(getClass().getClassLoader().getResource("images/" + msg.getPicture() + ".png").toString());
                ImageView profileImage = new ImageView(image);
                profileImage.setFitHeight(32);
                profileImage.setFitWidth(32);
                BubbledLabel bl6 = new BubbledLabel();
                if (msg.getType() == MessageType.VOICE) {
                    ImageView imageview = new ImageView(new Image(getClass().getClassLoader().getResource("images/sound.png").toString()));
                    bl6.setGraphic(imageview);
                    bl6.setText("Sent a voice message!");
                    VoicePlayback.playAudio(msg.getVoiceMsg());
                } else {
                    bl6.setText(msg.getName() + ": " + formMessage(Crypro.decrypt(Listener.key, msg.getMsg()), 90));
                }
                bl6.setBackground(new Background(new BackgroundFill(Color.AQUA, null, null)));
                HBox x = new HBox();
                bl6.setBubbleSpec(BubbleSpec.FACE_LEFT_CENTER);
                x.getChildren().addAll(profileImage, bl6);
                logger.debug("ONLINE USERS: " + Integer.toString(msg.getUserlist().size()));
                setOnlineLabel(Integer.toString(msg.getOnlineCount()));
                return x;
            }
        };
        othersMessages.setOnSucceeded(event -> {
            chatPane.getItems().add(othersMessages.getValue());
        });
        Thread t = new Thread(othersMessages);
        t.setDaemon(true);
        t.start();
    }

    private synchronized void printYourMessage(String msg, MessageType type) {
        Task<HBox> yourMessages = new Task<HBox>() {
            @Override
            public HBox call() throws Exception {
                Image image = userImageView.getImage();
                ImageView profileImage = new ImageView(image);
                profileImage.setFitHeight(32);
                profileImage.setFitWidth(32);
                BubbledLabel bl6 = new BubbledLabel();
                if (type == MessageType.VOICE) {
                    bl6.setGraphic(new ImageView(new Image(getClass().getClassLoader().getResource("images/sound.png").toString())));
                    bl6.setText("Sent a voice message!");
                } else {
                    bl6.setText(formMessage(msg, 90));
                }
                bl6.setBackground(new Background(new BackgroundFill(Color.LIGHTGREEN,
                        null, null)));
                HBox x = new HBox();
                x.setMaxWidth(chatPane.getWidth() - 20);
                x.setAlignment(Pos.TOP_RIGHT);
                bl6.setBubbleSpec(BubbleSpec.FACE_RIGHT_CENTER);
                x.getChildren().addAll(bl6, profileImage);
                return x;
            }
        };
        yourMessages.setOnSucceeded(event -> chatPane.getItems().add(yourMessages.getValue()));
        Thread t2 = new Thread(yourMessages);
        t2.setDaemon(true);
        t2.start();
    }

    public void setUserNameLabel(String username) {
        this.usernameLabel.setText(username);
    }

    public void setRoomNameLabel(String roomName) {
        this.roomnameLabel.setText(roomName);
    }

    public void setImageLabel(String selectedPicture) {
        switch (selectedPicture) {
            case "Man":
                this.userImageView.setImage(new Image(getClass().getClassLoader().getResource("images/man.png").toString()));
                break;
            case "Women":
                this.userImageView.setImage(new Image(getClass().getClassLoader().getResource("images/women.png").toString()));
                break;
            case "Default":
                this.userImageView.setImage(new Image(getClass().getClassLoader().getResource("images/default.png").toString()));
                break;
        }
    }

    private void setOnlineLabel(String usercount) {
        Platform.runLater(() -> onlineCountLabel.setText(usercount));
    }

    void setUserList(Message msg) {
        Platform.runLater(() -> {
            ObservableList<User> users = FXCollections.observableList(msg.getUsers());
            userList.setItems(users);
            userList.setCellFactory(new CellRenderer());
            setOnlineLabel(String.valueOf(msg.getUserlist().size()));
        });
    }

    void logoutScene() {
        if (!this.logout) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error!");
                alert.setHeaderText("Connection was broken off");
                alert.setContentText("Try to connect later!");
                alert.showAndWait();
            });
        }
        this.logout = false;

        Platform.runLater(() -> {
            FXMLLoader fmxlLoader = new FXMLLoader(getClass().getResource("/views/LoginView.fxml"));
            Parent window = null;
            try {
                window = (Pane) fmxlLoader.load();
            } catch (Exception e) {
                e.printStackTrace();
            }
            Stage stage = MainLauncher.getPrimaryStage();
            Scene scene = new Scene(window);
            stage.setResizable(false);
            stage.setWidth(350);
            stage.setHeight(420);
            stage.setScene(scene);
            stage.centerOnScreen();
        });
    }

    public void logoutApplication(MouseEvent mouseEvent) {
        this.logout = true;
        try {
            Listener.getInstance().socket.close();
//            Listener.getInstance().getInput().close();
//            Listener.oos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeApplication() {
        Platform.exit();
        System.exit(0);
    }
}