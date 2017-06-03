package com.client.login;
import com.client.chatwindow.ChatController;
import com.client.chatwindow.Listener;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.Random;
import java.util.ResourceBundle;

public class LoginController implements Initializable {
    @FXML private ImageView Defaultview;
    @FXML private ImageView Girlview;
    @FXML private ImageView Boyview;
    @FXML private ChoiceBox imagePicker;
    @FXML private Label selectedPicture;
    @FXML public  TextField roomNameTextfield;
    @FXML private TextField keyTextfield;
    @FXML private TextField usernameTextfield;
    @FXML private BorderPane borderPane;
    private static ChatController con;
    private double xOffset;
    private double yOffset;
    private Scene scene;

    private static final int PORT = 9001;
    private static final String HOSTNAME = "localhost";

    private static LoginController instance;
    private Thread x;

    public LoginController() {
        instance = this;
    }

    public static LoginController getInstance() {
        return instance;
    }

    public void loginButtonAction() throws IOException {
        String room = roomNameTextfield.getText();
        String key = keyTextfield.getText();
        String username = usernameTextfield.getText();
        if (username.equals("")){
            Platform.runLater(()-> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Error!");
                alert.setHeaderText("The user name isn't entered");
                alert.setContentText("Please enter user name.");
                alert.showAndWait();
            });
            return;
        }
        String picture = selectedPicture.getText();

        FXMLLoader fmxlLoader = new FXMLLoader(getClass().getResource("/views/ChatView.fxml"));
        Parent window = (Pane) fmxlLoader.load();
        con = fmxlLoader.<ChatController>getController();
        Listener listener = new Listener(HOSTNAME, PORT, username, picture, con, room, key);
        x = new Thread(listener);
        x.start();
        this.scene = new Scene(window);
    }

    public void showScene() throws IOException {
        Platform.runLater(() -> {
//            Stage stage = (Stage) hostnameTextfield.getScene().getWindow();
            Stage stage = (Stage) usernameTextfield.getScene().getWindow();
            stage.setResizable(true);
            stage.setWidth(1040);
            stage.setHeight(620);
            stage.setOnCloseRequest((WindowEvent e) -> {
                Platform.exit();
                System.exit(0);
            });
            stage.setScene(this.scene);
            stage.setMinWidth(800);
            stage.setMinHeight(300);
//            ResizeHelper.addResizeListener(stage);
            stage.centerOnScreen();
            con.setUserNameLabel("User: " + usernameTextfield.getText());
            con.setRoomNameLabel("ChatRoom: " + roomNameTextfield.getText());
            con.setImageLabel(selectedPicture.getText());
        });
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        imagePicker.getSelectionModel().selectFirst();
        selectedPicture.textProperty().bind(imagePicker.getSelectionModel().selectedItemProperty());
        selectedPicture.setVisible(false);

        /* Drag and Drop */
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

        imagePicker.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> selected, String oldPicture, String newPicture) {
                if (oldPicture != null) {
                    switch (oldPicture) {
                        case "Default":
                            Defaultview.setVisible(false);
                            break;
                        case "Man":
                            Boyview.setVisible(false);
                            break;
                        case "Women":
                            Girlview.setVisible(false);
                            break;
                    }
                }
                if (newPicture != null) {
                    switch (newPicture) {
                        case "Default":
                            Defaultview.setVisible(true);
                            break;
                        case "Man":
                            Boyview.setVisible(true);
                            break;
                        case "Women":
                            Girlview.setVisible(true);
                            break;
                    }
                }
            }
        });

        int numberOfSquares = 30;
        while (numberOfSquares > 0){
            generateAnimation();
            numberOfSquares--;
        }
    }

    /* This method is used to generate the animation on the login window */
    private void generateAnimation(){
        Random rand = new Random();
        int sizeOfSqaure = rand.nextInt(50) + 1;
        int speedOfSqaure = rand.nextInt(10) + 5;
        int startXPoint = rand.nextInt(420);
        int startYPoint = rand.nextInt(350);
        int direction = rand.nextInt(5) + 1;

        KeyValue moveXAxis = null;
        KeyValue moveYAxis = null;
        Rectangle r1 = null;

        switch (direction){
            case 1 :
                // MOVE LEFT TO RIGHT
                r1 = new Rectangle(0,startYPoint,sizeOfSqaure,sizeOfSqaure);
                moveXAxis = new KeyValue(r1.xProperty(), 350 -  sizeOfSqaure);
                break;
            case 2 :
                // MOVE TOP TO BOTTOM
                r1 = new Rectangle(startXPoint,0,sizeOfSqaure,sizeOfSqaure);
                moveYAxis = new KeyValue(r1.yProperty(), 420 - sizeOfSqaure);
                break;
            case 3 :
                // MOVE LEFT TO RIGHT, TOP TO BOTTOM
                r1 = new Rectangle(startXPoint,0,sizeOfSqaure,sizeOfSqaure);
                moveXAxis = new KeyValue(r1.xProperty(), 350 -  sizeOfSqaure);
                moveYAxis = new KeyValue(r1.yProperty(), 420 - sizeOfSqaure);
                break;
            case 4 :
                // MOVE BOTTOM TO TOP
                r1 = new Rectangle(startXPoint,420-sizeOfSqaure ,sizeOfSqaure,sizeOfSqaure);
                moveYAxis = new KeyValue(r1.xProperty(), 0);
                break;
            case 5 :
                // MOVE RIGHT TO LEFT
                r1 = new Rectangle(420-sizeOfSqaure,startYPoint,sizeOfSqaure,sizeOfSqaure);
                moveXAxis = new KeyValue(r1.xProperty(), 0);
                break;
            case 6 :
                //MOVE RIGHT TO LEFT, BOTTOM TO TOP
                r1 = new Rectangle(startXPoint,0,sizeOfSqaure,sizeOfSqaure);
                moveXAxis = new KeyValue(r1.xProperty(), 350 -  sizeOfSqaure);
                moveYAxis = new KeyValue(r1.yProperty(), 420 - sizeOfSqaure);
                break;

            default:
                System.out.println("default");
        }

        r1.setFill(Color.web("#F89406"));
        r1.setOpacity(0.1);

        KeyFrame keyFrame = new KeyFrame(Duration.millis(speedOfSqaure * 1000), moveXAxis, moveYAxis);
        Timeline timeline = new Timeline();
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.setAutoReverse(true);
        timeline.getKeyFrames().add(keyFrame);
        timeline.play();
        borderPane.getChildren().add(borderPane.getChildren().size()-1,r1);
    }

    /* Terminates Application */
    public void closeSystem(){
        Platform.exit();
        System.exit(0);
    }

    public void minimizeWindow(){
        MainLauncher.getPrimaryStage().setIconified(true);
    }
}
