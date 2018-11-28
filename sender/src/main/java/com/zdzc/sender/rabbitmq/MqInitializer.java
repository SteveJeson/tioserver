package com.zdzc.sender.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.zdzc.sender.Enum.ProtocolType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeoutException;

@Configuration
@Component
public class MqInitializer {

    @Value("${mq.server.hostname}")
    public String hostname;

    @Value("${mq.server.username}")
    public String username;

    @Value("${mq.server.password}")
    public String password;

    @Value("${mq.server.port}")
    public int port;

    @Value("${mq.server.net.interval}")
    public int interval;

    @Value("${protocol.type}")
    public String protocolType;

    @Value("${gps.connection.count}")
    public int gpsConnCount;

    @Value("${wrt.gps.connection.count}")
    public int wrtGpsConnCount;

    @Value("${gps.channel.count}")
    public int gpsChannelCount;

    @Value("${wrt.gps.channel.count}")
    public int wrtGpsChannelCount;

    @Value("${gps.queue.prefix}")
    public String gpsQueuePrefix;

    @Value("${wrt.gps.queue.prefix}")
    public String wrtGpsQueuePrefix;

    @Value("${gps.queue.count}")
    public int gpsQueueCount;

    @Value("${wrt.gps.queue.count}")
    public int wrtGpsQueueCount;

    @Value("${gps.queue.start}")
    public int gpsQueueStart;

    @Value("${wrt.gps.queue.start}")
    public int wrtGpsQueueStart;

    @Value("${alarm.connection.count}")
    public int alarmConnCount;

    @Value("${wrt.alarm.connection.count}")
    public int wrtAlarmConnCount;

    @Value("${alarm.channel.count}")
    public int alarmChannelCount;

    @Value("${wrt.alarm.channel.count}")
    public int wrtAlarmChannelCount;

    @Value("${alarm.queue.prefix}")
    public String alarmQueuePrefix;

    @Value("${wrt.alarm.queue.prefix}")
    public String wrtAlarmQueuePrefix;

    @Value("${alarm.queue.count}")
    public int alarmQueueCount;

    @Value("${wrt.alarm.queue.count}")
    public int wrtAlarmQueueCount;

    @Value("${alarm.queue.start}")
    public int alarmQueueStart;

    @Value("${wrt.alarm.queue.start}")
    public int wrtAlarmQueueStart;

    @Value("${heartbeat.connection.count}")
    public int heartbeatConnCount;

    @Value("${wrt.heartbeat.connection.count}")
    public int wrtHeartbeatConnCount;

    @Value("${heartbeat.channel.count}")
    public int heartbeatChannelCount;

    @Value("${wrt.heartbeat.channel.count}")
    public int wrtHeartbeatChannelCount;

    @Value("${heartbeat.queue.prefix}")
    public String heartbeatQueuePrefix;

    @Value("${wrt.heartbeat.queue.prefix}")
    public String wrtHeartbeatQueuePrefix;

    @Value("${heartbeat.queue.count}")
    public int heartbeatQueueCount;

    @Value("${wrt.heartbeat.queue.count}")
    public int wrtHeartbeatQueueCount;

    @Value("${heartbeat.queue.start}")
    public int heartbeatQueueStart;

    @Value("${wrt.heartbeat.queue.start}")
    public int wrtHeartbeatQueueStart;

    @Value("${business.connection.count}")
    public int businessConnCount;

    @Value("${business.channel.count}")
    public int businessChannelCount;

    @Value("${business.queue.prefix}")
    public String businessQueuePrefix;

    @Value("${business.queue.count}")
    public int businessQueueCount;

    @Value("${business.queue.start}")
    public int businessQueueStart;

    @Value("${wrt.controller.connection.count}")
    public int wrtControllerConnCount;

    @Value("${wrt.controller.channel.count}")
    public int wrtControllerChannelCount;

    @Value("${wrt.controller.queue.prefix}")
    public String wrtControllerQueuePrefix;

    @Value("${wrt.controller.queue.start}")
    public int wrtControllerQueueStart;

    public ConnectionFactory factory;

    public static final Logger logger = LoggerFactory.getLogger(MqInitializer.class);

    public CopyOnWriteArrayList<Channel> gpsChannels = new CopyOnWriteArrayList<>();

    public CopyOnWriteArrayList<Channel> wrtGpsChannels = new CopyOnWriteArrayList<>();

    public CopyOnWriteArrayList<Channel> alarmChannels = new CopyOnWriteArrayList<>();

    public CopyOnWriteArrayList<Channel> wrtAlarmChannels = new CopyOnWriteArrayList<>();

    public CopyOnWriteArrayList<Channel> heartbeatChannels = new CopyOnWriteArrayList<>();

    public CopyOnWriteArrayList<Channel> wrtHeartbeatChannels = new CopyOnWriteArrayList<>();

    public CopyOnWriteArrayList<Channel> wrtControllerChannels = new CopyOnWriteArrayList<>();

    public CopyOnWriteArrayList<Channel> businessChannels = new CopyOnWriteArrayList<>();

    public CopyOnWriteArrayList<Channel> replyChannels = new CopyOnWriteArrayList<>();

    /**
     * 配置MQ
     * @throws IOException
     * @throws TimeoutException
     */
    @Bean
    public void configMq() throws IOException, TimeoutException {
        setFactory();
        logger.info("Mq Server -> {}", hostname);
        if(ProtocolType.JT808.getValue().equals(protocolType)){
            logger.info("即将配置消息队列 -> {}", ProtocolType.JT808.getDesc());
            configJtMq();
        }else if(ProtocolType.WRT.getValue().equals(protocolType)){
            logger.info("即将配置消息队列 -> {}", ProtocolType.WRT.getDesc());
            configWrtMq();
        }

    }

    /**
     * 配置部标808协议消息队列
     */
    private void configJtMq() throws IOException, TimeoutException {
        //位置
        for(int i = 0;i < gpsConnCount;i++){
            logger.info("create GPS connection -> {}", i+1);
            createQueues(this.factory.newConnection(), gpsQueuePrefix, gpsChannelCount, gpsQueueCount, gpsQueueStart, gpsChannels);
        }

        //报警
        for(int i = 0;i < alarmConnCount;i++){
            logger.info("create ALARM connection -> {}", i+1);
            createQueues(this.factory.newConnection(), alarmQueuePrefix, alarmChannelCount, alarmQueueCount, alarmQueueStart, alarmChannels);
        }

        //心跳
        for(int i = 0;i < heartbeatConnCount;i++){
            logger.info("create HEARTBEAT connection -> {}", i+1);
            createQueues(this.factory.newConnection(), heartbeatQueuePrefix, heartbeatChannelCount, heartbeatQueueCount, heartbeatQueueStart, heartbeatChannels);
        }

        //业务
        for(int i = 0;i < businessConnCount;i++){
            logger.info("create BUSINESS connection -> {}", i+1);
            createQueues(this.factory.newConnection(), businessQueuePrefix, businessChannelCount, businessQueueCount, businessQueueStart, businessChannels);
        }
    }

    /**
     * 配置沃瑞特协议消息队列
     * @throws IOException
     * @throws TimeoutException
     */
    private void configWrtMq() throws IOException, TimeoutException {
        //位置
        for(int i = 0;i < wrtGpsConnCount;i++){
            logger.info("create GPS connection -> {}", i+1);
            createQueues(this.factory.newConnection(), wrtGpsQueuePrefix, wrtGpsChannelCount, wrtGpsQueueCount, wrtGpsQueueStart, wrtGpsChannels);
        }

        //报警
        for(int i =0;i < wrtAlarmConnCount;i++){
            logger.info("create ALARM connection -> {}", i+1);
            createQueues(this.factory.newConnection(), wrtAlarmQueuePrefix, wrtAlarmChannelCount, wrtAlarmQueueCount, wrtAlarmQueueStart, wrtAlarmChannels);
        }

        //心跳
        for(int i = 0;i < wrtHeartbeatConnCount;i++){
            logger.info("create HEARTBEAT connection -> {}", i+1);
            createQueues(this.factory.newConnection(), wrtHeartbeatQueuePrefix, wrtHeartbeatChannelCount, wrtHeartbeatQueueCount, wrtHeartbeatQueueStart, wrtHeartbeatChannels);
        }

        //控制器
        for(int i = 0;i < wrtControllerConnCount;i++){
            logger.info("create CONTROLLER connection -> {}", i+1);
            createQueues(this.factory.newConnection(), wrtControllerQueuePrefix, wrtControllerChannelCount, wrtControllerChannelCount, wrtControllerQueueStart, wrtControllerChannels);
        }

    }

    /**
     * Rabbitmq 工厂配置
     */
    private void setFactory(){
        this.factory = new ConnectionFactory();
        this.factory.setHost(hostname);
        this.factory.setUsername(username);
        this.factory.setPassword(password);
        this.factory.setPort(port);
        this.factory.setAutomaticRecoveryEnabled(true);
        this.factory.setNetworkRecoveryInterval(interval);
    }

    /**创建消息队列
     * @param connection 连接对象
     * @param QueuePrefix 队列前缀
     * @param channelCount 通道数量
     * @param queueCount 队列数量
     * @param queueStart 队列起始数
     * @param channels 通道集合
     */
    public void createQueues(Connection connection, String QueuePrefix, int channelCount, int queueCount, int queueStart, CopyOnWriteArrayList<Channel> channels){
        for(int i = 0;i < channelCount;i++){
            try {
                Channel channel = connection.createChannel();
                for(int j = 0;j < queueCount;j++){
                    String queueName = QueuePrefix + (queueStart + j);
                    channel.queueDeclare(queueName,true,false,false,null);
                }
               channels.add(channel);

            } catch (IOException e) {
                logger.error(e.getMessage());
            }
        }
    }


}
