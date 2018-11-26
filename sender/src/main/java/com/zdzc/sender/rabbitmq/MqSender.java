package com.zdzc.sender.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.MessageProperties;
import com.zdzc.sender.packet.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class MqSender {

    private static final Logger logger = LoggerFactory.getLogger(MqSender.class);


    public void send(Channel channel, byte[] sendBody, Message message, String qname){
        try {
            channel.basicPublish("", qname, MessageProperties.PERSISTENT_TEXT_PLAIN, sendBody);
//            sendToRemote(message.getAll());
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

//    private void sendToRemote(String message){
//        io.netty.channel.Channel channel = null;
//        try {
//            channel = channelPool.acquire().sync().get();
//            channel.write(ByteArrayUtil.hexStringToByteArray(message));
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } catch (ExecutionException e) {
//            e.printStackTrace();
//        } finally {
//            channelPool.release(channel);
//        }
//
//    }
}
