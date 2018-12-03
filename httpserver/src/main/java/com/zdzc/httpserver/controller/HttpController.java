package com.zdzc.httpserver.controller;

import com.alibaba.fastjson.JSONObject;
import org.tio.http.common.HttpRequest;
import org.tio.http.common.HttpResponse;
import org.tio.http.server.annotation.RequestPath;
import org.tio.http.server.util.Resps;
import org.tio.utils.resp.RespVo;


@RequestPath
public class HttpController {

    @RequestPath(value = "/OceanConnect")
    public HttpResponse json(HttpRequest request) throws Exception {
        JSONObject obj = JSONObject.parseObject(request.getBodyString());
        System.out.println(obj);
        HttpResponse ret = Resps.json(request, RespVo.ok());

        return ret;
    }
}
