package com.zdzc.sender.client;

import com.zdzc.sender.util.MsgEncoder;
import org.tio.client.intf.ClientAioHandler;
import org.tio.core.ChannelContext;
import org.tio.core.GroupContext;
import org.tio.core.exception.AioDecodeException;
import org.tio.core.intf.Packet;

import java.nio.ByteBuffer;

public class SenderClientAioHandler implements ClientAioHandler {
    @Override
    public Packet heartbeatPacket() {
        return null;
    }

    @Override
    public Packet decode(ByteBuffer buffer, int limit, int position, int readableLength, ChannelContext channelContext) throws AioDecodeException {
        return null;
    }

    @Override
    public ByteBuffer encode(Packet packet, GroupContext groupContext, ChannelContext channelContext) {
        return MsgEncoder.encode(packet, groupContext);
    }

    @Override
    public void handler(Packet packet, ChannelContext channelContext) throws Exception {

    }
}
