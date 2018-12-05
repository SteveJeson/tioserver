package com.zdzc.httpserver.controller;

import com.alibaba.fastjson.JSONObject;
import com.zdzc.httpserver.service.PacketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.http.common.HttpRequest;
import org.tio.http.common.HttpResponse;
import org.tio.http.server.annotation.RequestPath;
import org.tio.http.server.util.Resps;

import java.util.HashMap;
import java.util.Map;


@RequestPath
public class HttpController {

    private static Logger logger = LoggerFactory.getLogger(HttpController.class);

    @RequestPath(value = "/OceanConnect")
    public HttpResponse json(HttpRequest request) throws Exception {
        JSONObject obj = JSONObject.parseObject(request.getBodyString());
        HttpResponse ret = null;
        Map<String, Object> resultMap = new HashMap<>();
        try{
            PacketService.decode(obj);
            resultMap.put("statusCode", 200);
            ret = Resps.json(request, resultMap);
        }catch (Exception e){
            logger.error(e.getMessage());
            resultMap.put("statusCode", 500);
            ret = Resps.json(request, resultMap);
        }
        return ret;
    }

    /**
     * 获取真实IP
     * @param request 请求体
     * @return 真实IP
     */
    private static String getRealIp(HttpRequest request) {
        // 这个一般是Nginx反向代理设置的参数
        String ip = request.getHeader("X-Real-IP");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Forwarded-For");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemote().getIp();
        }
        // 处理多IP的情况（只取第一个IP）
        if (ip != null && ip.contains(",")) {
            String[] ipArray = ip.split(",");
            ip = ipArray[0];
        }
        return ip;
    }

}
