package com.zdzc.sender.client;

import org.tio.client.ClientChannelContext;
import org.tio.client.ClientGroupContext;
import org.tio.client.ReconnConf;
import org.tio.client.TioClient;
import org.tio.client.intf.ClientAioHandler;
import org.tio.client.intf.ClientAioListener;
import org.tio.core.Node;

public class SenderClientStarter {
    //用来自动连接的，不想自动连接请设为null
    private static ReconnConf reconnConf = new ReconnConf(60000L);

    private static ClientAioHandler tioClientHandler = new SenderClientAioHandler();
    private static ClientAioListener aioListener = new SenderClientAioListener();
    private static ClientGroupContext clientGroupContext = new ClientGroupContext(tioClientHandler, aioListener, reconnConf);

    private static TioClient tioClient = null;

    public static ClientChannelContext clientChannelContext;

    public static void start(String host, int port) throws Exception {
        clientGroupContext.setName("sender-tio-client");
        clientGroupContext.setHeartbeatTimeout(240000);//设为0或者负数则取消框架层面的心跳
        tioClient = new TioClient(clientGroupContext);
        Node serverNode = new Node(host, port);
        clientChannelContext = tioClient.connect(serverNode);
    }
}
