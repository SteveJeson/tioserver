package com.zdzc.common.coder;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

public class MsgDecoder {

    /**
     * 验证校验和(JT808协议)
     * @param data
     * @return
     */
    public static Boolean validateChecksum(byte[] data){
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
     * 计算校验和(JT808协议) -> 部标808
     *从开始标识符后一位到校验和前一位做异或运算
     * @param data
     * @param from
     * @param to
     * @return
     */
    public static int calculateChecksum(byte[] data, int from, int to){
        int cs = 0;
        for (int i = from; i < to; i++)
        {
            cs ^= data[i];

        }
        return cs;
    }

    /**
     * 转义还原(JT808协议)
     * 0x7d 0x01 -> 0x7d
     * 0x7d 0x02 -> 0x7e
     * @param data
     * @return
     */
    public static byte[] doReceiveEscape(byte[] data){
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
}
