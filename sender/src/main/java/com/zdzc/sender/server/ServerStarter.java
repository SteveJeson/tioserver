package com.zdzc.sender.server;

import com.zdzc.common.Enum.ProtocolType;
import com.zdzc.tcpclient.client.TcpClientStarter;
import org.apache.commons.lang.StringUtils;
import org.tio.server.ServerGroupContext;
import org.tio.server.TioServer;
import org.tio.server.intf.ServerAioHandler;
import org.tio.server.intf.ServerAioListener;
import org.tio.utils.jfinal.P;

public class ServerStarter {
    static ServerAioHandler aioHandler = new SenderServerAioHandler();
    static ServerAioListener aioListener = new SenderServerAioListener();
    static ServerGroupContext serverGroupContext = new ServerGroupContext("sender-tio-server", aioHandler, aioListener);
    public static void start() throws Exception {
        TioServer tioServer = new TioServer(serverGroupContext);
        String host = P.get("tio.server.host");
        int port = P.getInt("tio.server.port");
        //设置心跳超时
        int timeout = P.getInt("local.server.timeout");
        serverGroupContext.setHeartbeatTimeout(timeout);
        tioServer.start(host, port);

        if(StringUtils.equals(P.get("protocol.type"), ProtocolType.WRT.getValue())){
            return;
        }
        String remoteIp = P.get("remote.server.host");
        int remotePort = P.getInt("remote.server.port");
        TcpClientStarter.start(remoteIp, remotePort);
    }
}
