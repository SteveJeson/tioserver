package com.zdzc.httpserver;

import com.zdzc.httpserver.init.HttpServerInit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.utils.jfinal.P;

public class ServerStarter {
    private static Logger log = LoggerFactory.getLogger(ServerStarter.class);


    public static void main(String[] args) throws Exception {
        P.use("application.properties");

        HttpServerInit.init();
    }

    /**
     *
     * @author tanyaowu
     */
    public ServerStarter() {
    }
}
