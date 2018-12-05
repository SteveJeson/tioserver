package com.zdzc.httpserver.init;

import com.zdzc.httpserver.ServerStarter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.http.common.HttpConfig;
import org.tio.http.common.handler.HttpRequestHandler;
import org.tio.http.server.HttpServerStarter;
import org.tio.http.server.handler.DefaultHttpRequestHandler;
import org.tio.server.ServerGroupContext;
import org.tio.utils.jfinal.P;

public class HttpServerInit {
    private static Logger log = LoggerFactory.getLogger(HttpServerInit.class);

    public static HttpConfig httpConfig;

    public static HttpRequestHandler requestHandler;

    public static HttpServerStarter httpServerStarter;

    public static ServerGroupContext serverGroupContext;

    public static void init() throws Exception {
        //		long start = SystemTimer.currTime;

        int port = P.getInt("http.port");//启动端口
        httpConfig = new HttpConfig(port, null, null, null);
        httpConfig.setUseSession(false);
        httpConfig.setCheckHost(true);
        requestHandler = new DefaultHttpRequestHandler(httpConfig, ServerStarter.class);//第二个参数也可以是数组

        httpServerStarter = new HttpServerStarter(httpConfig, requestHandler);
        serverGroupContext = httpServerStarter.getServerGroupContext();
        serverGroupContext.setHeartbeatTimeout(0);
        httpServerStarter.start(); //启动http服务器
    }

}
