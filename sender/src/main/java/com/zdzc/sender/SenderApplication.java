package com.zdzc.sender;

import com.zdzc.sender.server.ServerStarter;
import org.tio.utils.jfinal.P;
import rabbitmq.MqInitializer;

import java.io.IOException;

public class SenderApplication {

    public static void main(String[] args) {
        P.use("application.properties");
        try {
            MqInitializer.init();
            ServerStarter.start();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
