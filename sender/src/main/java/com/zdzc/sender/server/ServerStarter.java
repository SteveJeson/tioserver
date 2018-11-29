package com.zdzc.sender.server;

import com.zdzc.sender.Enum.ProtocolType;
import com.zdzc.sender.client.SenderClientStarter;
import com.zdzc.sender.config.Config;
import org.apache.commons.lang.StringUtils;
import org.tio.server.ServerGroupContext;
import org.tio.server.TioServer;
import org.tio.server.intf.ServerAioHandler;
import org.tio.server.intf.ServerAioListener;

public class ServerStarter {
    static ServerAioHandler aioHandler = new SenderServerAioHandler();
    static ServerAioListener aioListener = new SenderServerAioListener();
    static ServerGroupContext serverGroupContext = new ServerGroupContext("sender-tio-server", aioHandler, aioListener);
    public static void start() throws Exception {
        TioServer tioServer = new TioServer(serverGroupContext);
        Config config = new Config("application.properties");
        String host = config.getValue("tio.server.host");
        int port = config.getValueInt("tio.server.port");
        //设置心跳超时
        int timeout = config.getValueInt("local.server.timeout");
        serverGroupContext.setHeartbeatTimeout(timeout);
        tioServer.start(host, port);

        if(StringUtils.equals(config.getValue("protocol.type"), ProtocolType.WRT.getValue())){
            return;
        }
        String remoteIp = config.getValue("remote.server.host");
        int remotePort = config.getValueInt("remote.server.port");
        SenderClientStarter.start(remoteIp, remotePort);
    }
}
