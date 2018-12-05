package com.zdzc.sender;

import com.zdzc.sender.server.ServerStarter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.utils.jfinal.P;
import rabbitmq.MqInitializer;

import java.io.IOException;

public class SenderApplication {
    private static final Logger logger = LoggerFactory.getLogger(SenderApplication.class);
    public static void main(String[] args) {
        P.use("application.properties");
        try {
            MqInitializer.init();
            ServerStarter.start();
            logger.info("====server started=====");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
