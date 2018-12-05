package com.zdzc.tcpclient.client;

import org.tio.client.ClientChannelContext;
import org.tio.client.ClientGroupContext;
import org.tio.client.ReconnConf;
import org.tio.client.TioClient;
import org.tio.client.intf.ClientAioHandler;
import org.tio.client.intf.ClientAioListener;
import org.tio.core.Node;

public class TcpClientStarter {
    //用来自动连接的，不想自动连接请设为null
    private static ReconnConf reconnConf = new ReconnConf(60000L);

    private static ClientAioHandler tioClientHandler = new TcpClientAioHandler();
    private static ClientAioListener aioListener = new TcpClientAioListener();
    private static ClientGroupContext clientGroupContext = new ClientGroupContext(tioClientHandler, aioListener, reconnConf);

    private static TioClient tioClient = null;

    public static ClientChannelContext clientChannelContext;

    public static void start(String host, int port) throws Exception {
        clientGroupContext.setName("tio-tcp-client");
        clientGroupContext.setHeartbeatTimeout(720000);//设为0或者负数则取消框架层面的心跳,720000为3分钟
        tioClient = new TioClient(clientGroupContext);
        Node serverNode = new Node(host, port);
        clientChannelContext = tioClient.connect(serverNode);
    }
}
