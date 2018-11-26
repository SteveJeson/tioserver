package com.zdzc.sender;

import com.zdzc.sender.server.ServerStarter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

@SpringBootApplication
public class SenderApplication {

    public static void main(String[] args) {
//        SpringApplication.run(SenderApplication.class, args);
        try {
            ServerStarter.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
