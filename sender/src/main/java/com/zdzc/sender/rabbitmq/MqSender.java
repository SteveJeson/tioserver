package com.zdzc.sender.rabbitmq;

import ch.qos.logback.core.encoder.ByteArrayUtil;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.MessageProperties;
import com.zdzc.common.Enum.ProtocolType;
import com.zdzc.sender.client.SenderClientStarter;
import com.zdzc.sender.packet.Message;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.Tio;

import java.io.IOException;

public class MqSender {

    private static final Logger logger = LoggerFactory.getLogger(MqSender.class);


    public void send(Channel channel, Message message, String queueName){
        try {
            channel.basicPublish("", queueName, MessageProperties.PERSISTENT_TEXT_PLAIN, message.getSendBody());
            //部标808协议转发到车管通平台
            if(StringUtils.equals(ProtocolType.JT808.getValue(), message.getHeader().getProtocolType())){
                sendToRemote(message.getAll());
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    /**
     * 转发一份消息到车管通平台 -> 部标808
     * @param message
     */
    private void sendToRemote(String message){
        Message sendBody = new Message();
        sendBody.setBody(ByteArrayUtil.hexStringToByteArray(message));
        Tio.send(SenderClientStarter.clientChannelContext, sendBody);
    }
}
