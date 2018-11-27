package com.zdzc.sender.rabbitmq;

import ch.qos.logback.core.encoder.ByteArrayUtil;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.MessageProperties;
import com.zdzc.sender.client.SenderClientStarter;
import com.zdzc.sender.packet.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.Tio;

import java.io.IOException;

public class MqSender {

    private static final Logger logger = LoggerFactory.getLogger(MqSender.class);


    public void send(Channel channel, byte[] sendBody, Message message, String qname){
        try {
            channel.basicPublish("", qname, MessageProperties.PERSISTENT_TEXT_PLAIN, sendBody);
            sendToRemote(message.getAll());
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    private void sendToRemote(String message){
        Message sendBody = new Message();
        sendBody.setBody(ByteArrayUtil.hexStringToByteArray(message));
        Tio.send(SenderClientStarter.clientChannelContext, sendBody);
    }
}
