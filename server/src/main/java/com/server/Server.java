 package com.server;

import com.entity.Room;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;

public class Server {

    private static final int PORT = 9001;
    private static Logger logger = LoggerFactory.getLogger(Server.class);
    static HashMap<String,Room> rooms = new HashMap<>();

    public static void main(String[] args) throws Exception {
        try (ServerSocket listener = new ServerSocket(PORT)) {
            logger.info("Server is running");
            while (true) {
                new Thread(new Handler(listener.accept())).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


