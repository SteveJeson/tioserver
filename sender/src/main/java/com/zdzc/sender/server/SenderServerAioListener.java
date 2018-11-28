package com.zdzc.sender.server;

import com.zdzc.sender.server.common.ServerSessionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.ChannelContext;
import org.tio.core.intf.Packet;
import org.tio.server.intf.ServerAioListener;

public class SenderServerAioListener implements ServerAioListener {

    private static final Logger logger = LoggerFactory.getLogger(SenderServerAioListener.class);

    @Override
    public void onAfterConnected(ChannelContext channelContext, boolean b, boolean b1) throws Exception {
        channelContext.setAttribute(new ServerSessionContext());
        int clients = channelContext.getGroupContext().connections.size();
        logger.info("client num -> " + clients);

    }

    @Override
    public void onAfterDecoded(ChannelContext channelContext, Packet packet, int i) throws Exception {
//        System.out.println("====after decoded==========");
    }

    @Override
    public void onAfterReceivedBytes(ChannelContext channelContext, int i) throws Exception {
//        System.out.println("======after received bytes=======");
    }

    @Override
    public void onAfterSent(ChannelContext channelContext, Packet packet, boolean b) throws Exception {
//        System.out.println("==========after sent==========");
    }

    @Override
    public void onAfterHandled(ChannelContext channelContext, Packet packet, long l) throws Exception {
//        System.out.println("============after handled=========");
    }

    @Override
    public void onBeforeClose(ChannelContext channelContext, Throwable throwable, String s, boolean b) throws Exception {
//        System.out.println("===============before close================");
    }
}
