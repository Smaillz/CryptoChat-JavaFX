package com.client.chatwindow;

import com.client.login.LoginController;
import com.messages.Message;
import com.messages.MessageType;
import com.messages.Status;
import com.security.Crypro;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import static com.messages.MessageType.CONNECTED;

public class Listener implements Runnable {
    private static String picture;
    Socket socket;
    private String hostname;
    private int port;

    static String username;
    private static String room;
    static String key;
    private static Listener listener;

    private ChatController controller;
    static ObjectOutputStream oos;
    private ObjectInputStream input;
    private Logger logger = LoggerFactory.getLogger(Listener.class);

    public Listener(String hostname, int port, String username, String picture, ChatController controller, String room, String key) {
        this.hostname = hostname;
        this.port = port;
        this.controller = controller;
        Listener.username = username;
        Listener.picture = picture;
        Listener.room = room;
        Listener.key = key;
        listener = this;
    }

    static Listener getInstance(){
        return listener;
    }

    public void run() {
        try {
            socket = new Socket(hostname, port);
            LoginController.getInstance().showScene();
            oos = new ObjectOutputStream(socket.getOutputStream());
            input = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            Platform.runLater(()-> {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Warning!");
                alert.setHeaderText("Could not connect to server");
                alert.setContentText("Please check for firewall issues and check if the server is running.");
                alert.showAndWait();
            });
            logger.error("Could not Connect");
            return;
        }
        logger.info("Connection accepted " + socket.getInetAddress() + ":" + socket.getPort());

        try {
            connectMessage();
            while (socket.isConnected()) {
                Message message = (Message) input.readObject();
                if (message != null) {
                    switch (message.getType()) {
                        case USER:
                            controller.printOtherMessage(message);
                            break;
                        case VOICE:
                            controller.printOtherMessage(message);
                            break;
                        case CONNECTED:
                            controller.newUserNotification(message);
                            controller.setUserList(message);
                            break;
                        case DISCONNECTED:
                            controller.setUserList(message);
                            break;
                        case STATUS:
                            controller.setUserList(message);
                            break;
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            controller.logoutScene();
        }
    }

//    This method is used for sending a normal Message
    static void send(String msg) throws IOException {
        Message createMessage = new Message();
        createMessage.setName(username);
        createMessage.setType(MessageType.USER);
        createMessage.setStatus(Status.AWAY);
//        createMessage.setMsg(msg);
        createMessage.setMsg(Crypro.encrypt(Listener.key,msg));
        createMessage.setPicture(picture);
        oos.writeObject(createMessage);
        oos.flush();
    }

//    This method is used for sending a voice Message
    public static void sendVoiceMessage(byte[] audio) throws IOException {
        Message createMessage = new Message();
        createMessage.setName(username);
        createMessage.setType(MessageType.VOICE);
        createMessage.setStatus(Status.AWAY);
        createMessage.setVoiceMsg(audio);
        createMessage.setPicture(picture);
        oos.writeObject(createMessage);
        oos.flush();
    }

//    This method is used for sending a normal Message about status update
    static void sendStatusUpdate(Status status) throws IOException {
        Message createMessage = new Message();
        createMessage.setName(username);
        createMessage.setType(MessageType.STATUS);
        createMessage.setStatus(status);
        createMessage.setPicture(picture);
        oos.writeObject(createMessage);
        oos.flush();
    }

//     This method is used to send a connecting message
    private static void connectMessage() throws IOException {
        Message connectMessage = new Message();
        connectMessage.setName(username);
        connectMessage.setType(CONNECTED);
        connectMessage.setRoom(room);
        connectMessage.setKey(key);
        connectMessage.setPicture(picture);
        oos.writeObject(connectMessage);
        oos.flush();
    }

//    ObjectInputStream getInput() {
//        return input;
//    }
}
