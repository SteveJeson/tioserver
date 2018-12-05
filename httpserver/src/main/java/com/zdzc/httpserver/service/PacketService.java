package com.zdzc.httpserver.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zdzc.common.coder.MsgDecoder;
import com.zdzc.common.utils.CommonUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class PacketService {

    private static Logger logger = LoggerFactory.getLogger(PacketService.class);

    public static void decode(JSONObject jsonObj) {
        List<String> list = new ArrayList<>();
        String nofifyType = jsonObj.get("notifyType").toString();
        if(StringUtils.equals("deviceDataChanged", nofifyType)){
            JSONObject service = jsonObj.getJSONObject("service");
            JSONObject data = service.getJSONObject("data");
            String msg = data.getString("UpDate");
            list.add(msg);
        }else if(StringUtils.equals("deviceDatasChanged", nofifyType)){
            JSONArray services = jsonObj.getJSONArray("services");
            for (int i = 0; i < services.size(); i++)
            {
                JSONObject json = services.getJSONObject(i);
                JSONObject data = json.getJSONObject("data");
                String msg = data.getString("UpDate");
                list.add(msg);
            }
        }else{
            logger.warn("unknown json data -> {}", jsonObj);
        }
        toMessageDecoder(list);
    }

    private static void toMessageDecoder(List<String> msgList){
        for (String message : msgList) {
            logger.info("source data -> {}", message);
            toMessageDecoder(message);
        }
    }

    private static void toMessageDecoder(String message){
        byte[] hexArr = MsgDecoder.doReceiveEscape(CommonUtil.toByteArray(message));
        boolean isValid = MsgDecoder.validateChecksum(hexArr);
        if(!isValid){
            logger.error("校验码验证错误, 转义后的数据 -> {}, 原始数据 -> {}", CommonUtil.toHexString(hexArr), message);
            return;
        }

    }

}
