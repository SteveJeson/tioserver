package com.zdzc.common.coder;

import com.zdzc.common.packet.Message;
import org.tio.core.GroupContext;
import org.tio.core.intf.Packet;

import java.nio.ByteBuffer;

public class MsgEncoder {
    public static ByteBuffer encode(Packet packet, GroupContext groupContext){
        Message message = (Message) packet;
        byte[] body = message.getBody();
        int bodyLen = 0;
        if (body != null) {
            bodyLen = body.length;
        }

        //创建一个新的bytebuffer
        ByteBuffer buffer = ByteBuffer.allocate(bodyLen);
        //设置字节序
        buffer.order(groupContext.getByteOrder());

        //写入消息体
        if (body != null) {
            buffer.put(body);
        }
        return buffer;
    }
}
