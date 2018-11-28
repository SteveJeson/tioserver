package com.zdzc.sender.server;

import ch.qos.logback.core.encoder.ByteArrayUtil;
import com.rabbitmq.client.Channel;
import com.zdzc.sender.Enum.DataType;
import com.zdzc.sender.Enum.ProtocolSign;
import com.zdzc.sender.packet.Header;
import com.zdzc.sender.packet.Message;
import com.zdzc.sender.rabbitmq.MqInitializer;
import com.zdzc.sender.rabbitmq.MqSender;
import com.zdzc.sender.util.*;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.ChannelContext;
import org.tio.core.GroupContext;
import org.tio.core.exception.AioDecodeException;
import org.tio.core.intf.Packet;
import org.tio.server.intf.ServerAioHandler;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class SenderServerAioHandler implements ServerAioHandler {
    private static final Logger logger = LoggerFactory.getLogger(SenderServerAioHandler.class);

    private MqInitializer mqInitializer = (MqInitializer) SpringContextUtil.getBean(MqInitializer.class);

    private MqSender mqSender;

    private static final AtomicInteger gpsNum = new AtomicInteger(0);
    private static final AtomicInteger alarmNum = new AtomicInteger(0);
    private static final AtomicInteger heartbeatNum = new AtomicInteger(0);

    public SenderServerAioHandler(){
        this.mqSender = new MqSender();
    }

    @Override
    public Message decode(ByteBuffer byteBuffer, int limit, int position, int readableLength, ChannelContext channelContext) throws AioDecodeException {
        Message msg = null;
        byte[] dst = new byte[readableLength];
        byteBuffer.get(dst);
        List<String> markList = Arrays.asList(ProtocolSign.JT808_BEGINMARK.getValue(), ProtocolSign.WRT_BEGINMARK.getValue());
        String beginMark = CommonUtil.toHexString(CommonUtil.subByteArr(dst,0,1));
        String wrtBeginMark;
        try {
            wrtBeginMark = new String(CommonUtil.subByteArr(dst, 0, 3), "UTF-8");
            if(!markList.contains(beginMark.toUpperCase()) && !markList.contains(wrtBeginMark.toUpperCase())){
                String hexStr = CommonUtil.toHexString(dst);
                String str = new String(dst, "UTF-8");
                logger.warn("未知协议内容: 十六进制字符串形式 -> {}, 普通字符串形式 -> {}", hexStr, str);
                return null;
            }
        } catch (UnsupportedEncodingException e) {
            logger.error(e.getStackTrace().toString());
            return null;
        }

        if(markList.get(0).equals(beginMark.toUpperCase())){
            String hexStr = CommonUtil.toHexString(dst);
            logger.debug("source data -> {}", hexStr);
            msg = toJt808Decoder(dst);
        }else if(markList.get(1).equals(wrtBeginMark.toUpperCase())){
            try {
                String str = new String(dst, "UTF-8");
                logger.debug("source data -> {}", str);
                msg = toWrtDecoder(str);
            } catch (UnsupportedEncodingException e) {
                logger.error(e.getStackTrace().toString());
            }

        }
        return msg;
    }

    @Override
    public ByteBuffer encode(Packet packet, GroupContext groupContext, ChannelContext channelContext) {
        return MsgEncoder.encode(packet, groupContext);
    }

    @Override
    public void handler(Packet packet, ChannelContext channelContext) throws Exception {
        Message msg = (Message)packet;
        if(msg.getStick()){
            logger.info("handle stick message -> {}", msg.getAll());
            List<String> list = dealPackageSplicing(msg.getAll());
            for (String data : list){
                msg = decodeMessage(data);
                logger.info("msg -> {}", msg.getAll());
            }
        }

        //给客户端发送应答消息
        /*if(msg.getReplyBody() != null){
            Message responseBody = new Message();
            responseBody.setBody(msg.getReplyBody());
            Tio.send(channelContext, responseBody);
        }

        if(msg.getExtReplyBody() != null){
            Message responseBody = new Message();
            responseBody.setBody(msg.getExtReplyBody());
            Tio.send(channelContext, responseBody);
        }

        //将收到的定位、报警、心跳消息推送至Rabbitmq
        int msgType = msg.getHeader().getMsgType();
        if(msgType == DataType.GPS.getValue()){
            //定位
            toSendGpsMessage(msg);
        }else if(msgType == DataType.ALARM.getValue()){
            //报警
            toSendAlarmMessage(msg);

        }else if(msgType == DataType.HEARTBEAT.getValue()){
            //心跳
            toSendHeartBeatMessage(msg);
        }*/

    }

    /**
     * JT808协议数据解析
     * @param data
     */
    private Message toJt808Decoder(byte[] data){
        String hexStr = CommonUtil.toHexString(data);
        byte[] bs = doReceiveEscape(data);
        String hex = CommonUtil.toHexString(bs);
        Boolean isValid = validateChecksum(bs);
        if(!isValid){
            logger.error("校验码验证错误, 转义后的数据 -> {}, 原始数据 -> {}", hex, hexStr);
            return null;
        }
        return decodeMessage(data);
    }

    /**
     * 沃瑞特C11协议数据解析
     * @param data
     * @return
     */
    private Message toWrtDecoder(String data){
        if (data != null && data.startsWith(ProtocolSign.WRT_BEGINMARK.getValue())
                && data.endsWith(ProtocolSign.WRT_ENDMARK.getValue())
                && !data.contains(ProtocolSign.WRT_ENDMARK.getValue()+ProtocolSign.WRT_BEGINMARK.getValue())) {
            //直接处理
            return decodeMessage(data);
        } else {
            //粘包数据
            Message message = new Message();
            message.setStick(true);
            message.setAll(data);
            return message;
        }
    }

    private static List<String> dealPackageSplicing(String info){
        List<String> messages = new ArrayList<>();
        String beginMark = ProtocolSign.WRT_BEGINMARK.getValue();
        String endMark = ProtocolSign.WRT_ENDMARK.getValue();
        String[] msgArr = info.split(endMark + beginMark);
        if (msgArr.length != 1) {
            for (int i = 0; i < msgArr.length; i++) {
                String message;
                if (i == 0) {
                    message = msgArr[i] + endMark;
                } else if (i == msgArr.length - 1) {
                    message = beginMark + msgArr[i];
                } else {
                    message = beginMark + msgArr[i] + endMark;
                }
                messages.add(message);
            }
        }else {
            logger.warn("unknown message: " + info);
        }
        return messages;
    }

    /**
     * 转义还原(JT808协议)
     * 0x7d 0x01 -> 0x7d
     * 0x7d 0x02 -> 0x7e
     * @param data
     * @return
     */
    private byte[] doReceiveEscape(byte[] data){
        List<Byte> list = new LinkedList<>();
        for (int i = 0; i < data.length; i++)
        {
            if (data[i] == 0x7d && data[i + 1] == 0x01)
            {
                list.add((byte)0x7d);
                i++;
            }
            else if (data[i] == 0x7d && data[i + 1] == 0x02)
            {
                list.add((byte)0x7e);
                i++;
            }
            else
            {
                list.add(data[i]);
            }
        }
        ByteBuffer bb = ByteBuffer.allocate(list.size());
        for (Byte b : list) {
            bb.put(b);
        }
        return bb.array();
    }

    /**
     * 验证校验和(JT808协议)
     * @param data
     * @return
     */
    private Boolean validateChecksum(byte[] data){
        // 1. 去掉分隔符之后，最后一位就是校验码
        int checkSumInPkg = data[data.length - 1 - 1];
        int calculatedCheckSum = calculateChecksum(data, 1, data.length - 1 - 1);
        if (checkSumInPkg != calculatedCheckSum)
        {
            return false;
        }
        return true;
    }

    /**
     * 计算校验和(JT808协议)
     *从开始标识符后一位到校验和前一位做异或运算
     * @param data
     * @param from
     * @param to
     * @return
     */
    private int calculateChecksum(byte[] data, int from, int to){
        int cs = 0;
        for (int i = from; i < to; i++)
        {
            cs ^= data[i];

        }
        return cs;
    }

    /**
     * 解析消息 -> 部标808
     * @param data
     * @return
     */
    private Message decodeMessage(byte[] data){
        //设置消息头
        Header header = new Header();
        decodeHeader(data, header);
        Message message = new Message();
        message.setHeader(header);//设置消息头
        int msgBodyByteStartIndex = 12 + 1;
        // 3. 消息体
        // 有子包信息,消息体起始字节后移四个字节:消息包总数(word(16))+包序号(word(16))
        if (header.hasSubPackage())
        {
            msgBodyByteStartIndex = 16 + 1;
        }
        //设置消息体
        byte[] buffer = new byte[header.getMsgBodyLength()];
        System.arraycopy(data, msgBodyByteStartIndex, buffer, 0,header.getMsgBodyLength());
        message.setBody(buffer);//设置消息体
        message.setAll(CommonUtil.toHexString(data));

        //设置应答消息
        setReplyBodyAndType(message);
        return message;
    }

    /**
     * 解析消息 -> 沃瑞特C11
     * @param data
     * @return
     */
    private Message decodeMessage(String data){
        //设置消息头
        Header header = new Header();
        decodeHeader(data, header);
        Message message = new Message();
        message.setHeader(header);
        message.setStick(false);
        message.setAll(data);
        //设置消息体
        String body = data.substring(7, data.length()-1);
        message.setBody(body.getBytes());
        //设置应答消息
        setReplyMessage(message);
        return message;
    }

    /**
     * 解析消息头 -> 部标808
     * @param data
     * @param header
     */
    private void decodeHeader(byte[] data, Header header){
        int msgId = CommonUtil.cutBytesToInt(data, 1, 2);
        int msgBodyProps = CommonUtil.cutBytesToInt(data, 2 + 1, 2);
        boolean hasSubPackage = (((msgBodyProps & 0x2000) >> 13) == 1);
        int msgBodyLength = (CommonUtil.cutBytesToInt(data, 2 + 1, 2) & 0x3ff);
        String terminalPhone = CommonUtil.toHexString(CommonUtil.subByteArr(data, 5, 6));
        int flowId = CommonUtil.cutBytesToInt(data, 11, 2);
        header.setMsgId(msgId);
        header.setMsgBodyProps(msgBodyProps);
        header.setHasSubPackage(hasSubPackage);
        header.setMsgBodyLength(msgBodyLength);
        header.setTerminalPhone(terminalPhone);
        header.setMsgLength(data.length);
        header.setProtocolType(CommonUtil.toHexString(CommonUtil.subByteArr(data, 0, 1)));
        header.setFlowId(flowId);
    }

    /**
     * 解析消息头 -> 沃瑞特C11
     * @param data
     * @param header
     */
    private void decodeHeader(String data, Header header){
        String msgIdStr = data.substring(3, 7);
        header.setMsgIdStr(msgIdStr);
        if(StringUtils.equals(Command.WRT_MSG_ID_LOGIN, msgIdStr)){
            String terminalPhone = data.substring(7, 22);
            header.setTerminalPhone(terminalPhone);
        }
    }

    /**
     * 设置应答消息 -> 部标808
     * @param message
     */
    private void setReplyBodyAndType(Message message){
        int msgId = message.getHeader().getMsgId();
        String terminalPhone = message.getHeader().getTerminalPhone();
        int flowId = message.getHeader().getFlowId();
        byte[] body = message.getBody();
        String all = message.getAll();
        if (msgId == Command.MSG_ID_TERMINAL_REGISTER) {
            logger.debug("【808】终端注册 -> " + all);
            //1. 终端注册 ==> 终端注册应答
            byte[] sendMsg = newRegistryReplyMsg(0014, terminalPhone, flowId);
            message.setReplyBody(sendMsg);
            message.getHeader().setMsgType(DataType.Registry.getValue());
        }else if (msgId == Command.MSG_ID_TERMINAL_AUTHENTICATION) {
            logger.debug("【808】终端鉴权 -> " + all);
            //2. 终端鉴权 ==> 平台通用应答
            byte[] sendMsg = newCommonReplyMsg(0005, terminalPhone, flowId, msgId);
            message.setReplyBody(sendMsg);
            //查询终端属性
            byte[] sendBody = newQueryPropReplyMsg(0005, terminalPhone, flowId);
            message.setExtReplyBody(sendBody);
            message.getHeader().setMsgType(DataType.Authentication.getValue());
        }else if (msgId == Command.MSG_ID_TERMINAL_HEART_BEAT) {
            //3. 终端心跳-消息体为空 ==> 平台通用应答
            logger.debug("【808】终端心跳 -> " + all);
            SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmss");
            String time = sdf.format(new Date());
            byte[] msgBody = ByteArrayUtil.hexStringToByteArray(time);//自定义心跳body为当前时间
            //客户端消息应答
            byte[] sendMsg = newCommonReplyMsg(0005, terminalPhone, flowId, msgId);
            //设置回复信息
            message.setBody(msgBody);
            message.setReplyBody(sendMsg);
            message.getHeader().setMsgType(DataType.HEARTBEAT.getValue());
        }else if (msgId == Command.MSG_ID_TERMINAL_LOCATION_INFO_UPLOAD) {
            logger.debug("【808】终端定位（单个）-> " + all);
            //4. 位置信息汇报 ==> 平台通用应答
            byte[] sendMsg = newCommonReplyMsg(0005, terminalPhone, flowId, msgId);
            message.setReplyBody(sendMsg);
            int alarmSign = CommonUtil.cutBytesToInt(body, 0, 4);
            message.getHeader().setMsgType(alarmSign <= 0?DataType.GPS.getValue():DataType.ALARM.getValue());
        }else if (msgId == Command.MSG_ID_TERMINAL_LOCATION_INFO_BATCH_UPLOAD) {
            logger.debug("【808】终端定位（批量）-> " + all);
            //5.定位数据批量上传0x0704协议解析
            byte[] sendMsg = newCommonReplyMsg(0005, terminalPhone, flowId, msgId);
            message.setReplyBody(sendMsg);
            byte[] mb = CommonUtil.subByteArr(body, 5, body.length - 5);
            int alarmSign = CommonUtil.cutBytesToInt(mb, 0, 4);
            message.getHeader().setMsgType(alarmSign <= 0?DataType.GPS.getValue(): DataType.ALARM.getValue());
        }else if (msgId == Command.MSG_ID_TERMINAL_PROP_QUERY_RESP) {
            logger.debug("【808】终端属性查询应答 -> " + all);
            //6.终端属性应答消息
            byte[] msgType = new byte[1];
            msgType[0] = 02;
            byte[] newBodyByte = CommonUtil.bytesMerge(msgType, body);
            message.setBody(newBodyByte);
            message.getHeader().setMsgType(DataType.Property.getValue());
        }else {
            logger.error("【808】未知消息类型，终端手机号 -> "+terminalPhone);
        }
    }

    /**
     * 设置应答消息 -> 沃瑞特C11
     * @param message
     */
    private void setReplyMessage(Message message){
        String msgIdStr = message.getHeader().getMsgIdStr();
        String beginMark = ProtocolSign.WRT_BEGINMARK.getValue();
        String endMark = ProtocolSign.WRT_ENDMARK.getValue();
        String resp;
        switch (msgIdStr){
            case Command.WRT_MSG_ID_LOGIN:
                String date = DateFormatUtils.format(new Date(DateUtil.getUTCTime()), "yyyyMMddHHmmss");
                resp = Command.WRT_MSG_LOGIN_RESP + date;
                break;
            case Command.WRT_MSG_ID_TERMINAL_LOCATION:
                resp = Command.WRT_MSG_LOCATION_RESP;
                break;
            case Command.WRT_MSG_ID_TERMINAL_ALARM:
                resp = Command.WRT_MSG_ALARM_RESP;
                break;
            case Command.WRT_MSG_ID_TERMINAL_HEARTBEAT:
                resp = Command.WRT_MSG_HEARTBEAT_RESP;
                break;
            case Command.WRT_MSG_ID_TERMINAL_CONTROLLER:
                resp = Command.WRT_MSG_CONTROLLER_RESP;
                break;
            case Command.WRT_MSG_ID_TERMINAL_STATUS:
                resp = Command.WRT_MSG_STATUS_RESP;
                break;
            case Command.WRT_MSG_ID_TERMINAL_IMSI:
                resp = Command.WRT_MSG_IMSI_RESP;
                break;
            default:
                resp = "";
                logger.warn("位置的消息ID -> {}", msgIdStr);
                break;
        }
        if(StringUtils.isEmpty(resp)){
            return;
        }
        String replyMsg = beginMark + resp + endMark;
        message.setReplyBody(replyMsg.getBytes());
    }

    /**
     * 终端注册消息应答
     * @param msgBodyProps
     * @param phone
     * @param flowId
     * @return
     */
    public byte[] newRegistryReplyMsg(int msgBodyProps, String phone, int flowId)
    {
        //7E
        //8100            消息ID
        //0004            消息体属性
        //018512345678    手机号
        //0015            消息流水号
        //0015            应答流水号
        //04              结果(00成功, 01车辆已被注册, 02数据库中无该车辆, 03终端已被注册, 04数据库中无该终端)  无车辆与无终端有什么区别 ?
        //313C             鉴权码
        //7E
        int len = 0;
        // 1. 0x7e
        byte[] bt1 = CommonUtil.integerTo1Bytes(Command.PKG_DELIMITER);
        len += bt1.length;
        // 2. 消息ID word(16)
        byte[] bt2 = CommonUtil.integerTo2Bytes(Command.CMD_TERMINAL_REGISTER_RESP);
        len += bt2.length;
        // 3.消息体属性
        byte[] bt3 = CommonUtil.integerTo2Bytes(msgBodyProps);
        len += bt3.length;
        // 4. 终端手机号 bcd[6]
        byte[] bt4 = CommonUtil.string2Bcd(phone);
        len += bt4.length;
        // 5. 消息流水号 word(16),按发送顺序从 0 开始循环累加
        byte[] bt5 = CommonUtil.integerTo2Bytes(flowId);
        len += bt5.length;
        // 6. 应答流水号
        byte[] bt6 = CommonUtil.integerTo2Bytes(flowId);
        len += bt6.length;
        // 7. 成功
        byte[] bt7 = CommonUtil.integerTo1Bytes(0);
        len += bt7.length;
        // 8. 鉴权码
        byte[] bt8 = new byte[0];
        try {
            bt8 = Command.REPLYTOKEN.getBytes(Command.STRING_ENCODING);
        } catch (UnsupportedEncodingException e) {
            logger.error("replytoken parse error: "+e.getMessage());
        }
        len += bt8.length;
        ByteBuffer buffer = ByteBuffer.allocate(len);
        buffer.put(bt1);
        buffer.put(bt2);
        buffer.put(bt3);
        buffer.put(bt4);
        buffer.put(bt5);
        buffer.put(bt6);
        buffer.put(bt7);
        buffer.put(bt8);
        // 校验码
        int checkSum = calculateChecksum(buffer.array(), 1, buffer.array().length);
        byte[] bt9 = CommonUtil.integerTo1Bytes(checkSum);
        len += bt9.length;
        len += bt1.length;
        ByteBuffer buf = ByteBuffer.allocate(len);
        buf.put(buffer.array());
        buf.put(bt9);
        //结束符
        buf.put(bt1);

        // 转义
        return doSendEscape(buf.array(), 1, buf.array().length - 1);
    }

    /**
     * 发送消息时转义
     * @param data
     * @param start
     * @param end
     * @return
     */
    private static byte[] doSendEscape(byte[] data, int start, int end)
    {
        List<Byte> list = new LinkedList<>();
        for (int i = 0; i < start; i++)
        {
            list.add(data[i]);
        }
        for (int i = start; i < end; i++)
        {
            if (data[i] == 0x7e)
            {
                list.add((byte)0x7d);
                list.add((byte)0x02);
            }
            else
            {
                list.add(data[i]);
            }
        }
        for (int i = end; i < data.length; i++)
        {
            list.add(data[i]);
        }
        ByteBuffer buffer = ByteBuffer.allocate(list.size());
        for (Byte b : list) {
            buffer.put(b);
        }
        return buffer.array();
    }

    /**
     * 通用消息应答
     * @param msgBodyProps
     * @param phone
     * @param flowId
     * @param msgId
     * @return
     */
    public byte[] newCommonReplyMsg(int msgBodyProps, String phone, int flowId, int msgId)
    {
        //7E
        //8100            消息ID
        //0004            消息体属性
        //018512345678    手机号
        //0015            消息流水号
        //0015            应答流水号
        //04              结果(00成功, 01车辆已被注册, 02数据库中无该车辆, 03终端已被注册, 04数据库中无该终端)  无车辆与无终端有什么区别 ?
        //313C             鉴权码
        //7E
        int len = 0;
        // 1. 0x7e
        byte[] bt1 = CommonUtil.integerTo1Bytes(Command.PKG_DELIMITER);
        len += bt1.length;
        // 2. 消息ID word(16)
        byte[] bt2 = CommonUtil.integerTo2Bytes(Command.CMD_COMMON_RESP);
        len += bt2.length;
        // 3.消息体属性
        byte[] bt3 = CommonUtil.integerTo2Bytes(msgBodyProps);
        len += bt3.length;
        // 4. 终端手机号 bcd[6]
        byte[] bt4 = CommonUtil.string2Bcd(phone);
        len += bt4.length;
        // 5. 消息流水号 word(16),按发送顺序从 0 开始循环累加
        byte[] bt5 = CommonUtil.integerTo2Bytes(flowId);
        len += bt5.length;
        // 6. 应答流水号
        byte[] bt6 = CommonUtil.integerTo2Bytes(flowId);
        len += bt6.length;
        // 7. 对应终端消息ID
        byte[] bt7 = CommonUtil.integerTo2Bytes(msgId);
        len += bt7.length;
        // 8. 成功
        byte[] bt8 = CommonUtil.integerTo1Bytes(0);
        len += bt8.length;

        ByteBuffer buffer = ByteBuffer.allocate(len);
        buffer.put(bt1);
        buffer.put(bt2);
        buffer.put(bt3);
        buffer.put(bt4);
        buffer.put(bt5);
        buffer.put(bt6);
        buffer.put(bt7);
        buffer.put(bt8);
        // 校验码
        int checkSum = calculateChecksum(buffer.array(), 1, buffer.array().length);
        byte[] bt9 = CommonUtil.integerTo1Bytes(checkSum);
        len += bt9.length;
        len += bt1.length;
        ByteBuffer buf = ByteBuffer.allocate(len);
        buf.put(buffer.array());
        buf.put(bt9);
        //结束符
        buf.put(bt1);
        // 转义
        return doSendEscape(buf.array(), 1, buf.array().length - 1);
    }

    /**
     * 终端鉴权 ==> 查询终端属性
     * @param msgBodyProps
     * @param phone
     * @param flowId
     * @return
     */
    public byte[] newQueryPropReplyMsg(int msgBodyProps, String phone, int flowId){
        int len = 0;
        // 1. 0x7e
        byte[] bt1 = CommonUtil.integerTo1Bytes(Command.PKG_DELIMITER);
        len += bt1.length;
        // 2. 消息ID word(16)
        byte[] bt2 = CommonUtil.integerTo2Bytes(Command.CMD_TERMINAL_PROP_QUERY);
        len += bt2.length;
        // 3.消息体属性
        byte[] bt3 = CommonUtil.integerTo2Bytes(msgBodyProps);
        len += bt3.length;
        // 4. 终端手机号 bcd[6]
        byte[] bt4 = CommonUtil.string2Bcd(phone);
        len += bt4.length;
        // 5. 消息流水号 word(16),按发送顺序从 0 开始循环累加
        byte[] bt5 = CommonUtil.integerTo2Bytes(flowId);
        len += bt5.length;
        ByteBuffer buffer = ByteBuffer.allocate(len);
        buffer.put(bt1);
        buffer.put(bt2);
        buffer.put(bt3);
        buffer.put(bt4);
        buffer.put(bt5);

        // 6.校验码
        int checkSum = calculateChecksum(buffer.array(), 1, buffer.array().length);
        byte[] bt9 = CommonUtil.integerTo1Bytes(checkSum);
        len += bt9.length;
        len += bt1.length;
        ByteBuffer buf = ByteBuffer.allocate(len);
        buf.put(buffer.array());
        buf.put(bt9);
        // 7. 0x7e
        buf.put(bt1);

        // 转义
        return doSendEscape(buf.array(), 1, buf.array().length - 1);
    }

    private void toSendGpsMessage(Message message){
        gpsNum.incrementAndGet();
        CopyOnWriteArrayList<Channel> channels = mqInitializer.gpsChannels;
        Channel channel = channels.get(gpsNum.intValue() % channels.size());
        String queueName = mqInitializer.gpsQueuePrefix + (gpsNum.intValue() % mqInitializer.gpsQueueCount + 1);
        byte[] sign = new byte[1];
        sign[0] = 01;
        byte[] newBody = CommonUtil.bytesMerge(sign, message.getBody());
        byte[] sendMsg = CommonUtil.bytesMerge(
                ByteArrayUtil.hexStringToByteArray(message.getHeader().getTerminalPhone()), newBody);
        String hex = ByteArrayUtil.toHexString(sendMsg);
        mqSender.send(channel, hex.getBytes(Charset.forName("UTF-8")), message, queueName);
    }

    private void toSendAlarmMessage(Message message){
        alarmNum.incrementAndGet();
        CopyOnWriteArrayList<Channel> channels = mqInitializer.alarmChannels;
        Channel channel = channels.get(alarmNum.intValue() % channels.size());
        String queueName = mqInitializer.alarmQueuePrefix + (alarmNum.intValue() % mqInitializer.alarmQueueCount + 1);
        byte[] sendMsg = CommonUtil.bytesMerge(ByteArrayUtil
                .hexStringToByteArray(message.getHeader().getTerminalPhone()), message.getBody());
        String hex = ByteArrayUtil.toHexString(sendMsg);
        mqSender.send(channel, hex.getBytes(Charset.forName("UTF-8")), message, queueName);

        //报警推送到电动车平台MQ一份
        CopyOnWriteArrayList<Channel> chs = mqInitializer.businessChannels;
        Channel ch = chs.get(alarmNum.intValue() % chs.size());
        String qn = mqInitializer.businessQueuePrefix +
                (alarmNum.intValue() % mqInitializer.businessQueueCount + mqInitializer.businessQueueStart);
        mqSender.send(ch, hex.getBytes(Charset.forName("UTF-8")), message, qn);
    }

    private void toSendHeartBeatMessage(Message message){
        heartbeatNum.incrementAndGet();
        CopyOnWriteArrayList<Channel> channels = mqInitializer.heartbeatChannels;
        Channel channel = channels.get(heartbeatNum.intValue() % channels.size());
        String queueName = mqInitializer.heartbeatQueuePrefix + (heartbeatNum.intValue() % mqInitializer.heartbeatQueueCount + 1);
        byte[] sendMsg = CommonUtil.bytesMerge(ByteArrayUtil
                .hexStringToByteArray(message.getHeader().getTerminalPhone()), message.getBody());
        String hex = ByteArrayUtil.toHexString(sendMsg);
        mqSender.send(channel, hex.getBytes(Charset.forName("UTF-8")), message, queueName);
    }
}
