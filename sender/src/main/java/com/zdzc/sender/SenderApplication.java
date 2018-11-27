package com.zdzc.sender;

import com.zdzc.sender.server.ServerStarter;
import com.zdzc.sender.util.SpringContextUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.io.IOException;

@SpringBootApplication
public class SenderApplication {

    public static void main(String[] args) {
        ApplicationContext context =  SpringApplication.run(SenderApplication.class, args);
        SpringContextUtil.setApplicationContext(context);
        try {
            ServerStarter.start();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
