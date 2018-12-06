package com.zdzc.httpserver;

import com.zdzc.httpserver.init.HttpServerInit;
import com.zdzc.tcpclient.client.TcpClientStarter;
import org.tio.utils.jfinal.P;
import rabbitmq.MqInitializer;

public class ServerStarter {

    public static void main(String[] args) throws Exception {
        P.use("application.properties");
        HttpServerInit.init();
        MqInitializer.init();//初始化MQ
        String remoteIp = P.get("remote.server.host");
        int remotePort = P.getInt("remote.server.port");
        TcpClientStarter.start(remoteIp, remotePort);
    }

    public ServerStarter() {
    }
}
