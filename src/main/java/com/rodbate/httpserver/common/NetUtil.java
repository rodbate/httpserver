package com.rodbate.httpserver.common;




import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static com.rodbate.httpserver.common.ServerConfig.*;
import static com.rodbate.httpserver.common.StringUtil.*;


public class NetUtil {


    private static final String hostname = getProperty("hostname");

    private static final String PORT = getProperty("port");

    private static final Logger LOGGER = LoggerFactory.getLogger(NetUtil.class);

    public static String getHttpHeaderHost(){

        String host = null;

        if (isNull(hostname)) {
            try {
                host = InetAddress.getLocalHost().getHostName() + ("80".equals(PORT) ? "" : ":" + PORT);
            } catch (UnknownHostException e) {
                e.printStackTrace();
                LOGGER.error(e.getMessage(), e);
            }

        } else {

            try {
                host = InetAddress.getByName(hostname).getHostName() + ("80".equals(PORT) ? "" : ":" + PORT);
            } catch (UnknownHostException e) {
                e.printStackTrace();
                LOGGER.error(e.getMessage(), e);
            }
        }


        return host;
    }
}
