package cn.sh.ideal.job.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * net util
 * <p>Copy from <a href="https://github.com/xuxueli/xxl-job/blob/master/xxl-job-core/src/main/java/com/xxl/job/core/util/NetUtil.java">xxl-job IpUtil</a></p></p>
 */
public class NetUtil {
    private static final Logger log = LoggerFactory.getLogger(NetUtil.class);

    /**
     * find available port
     */
    public static int findAvailablePort(int defaultPort) {
        int portTmp = defaultPort;
        while (portTmp < 65535) {
            if (!isPortUsed(portTmp)) {
                return portTmp;
            } else {
                portTmp++;
            }
        }
        portTmp = defaultPort - 1;
        while (portTmp > 0) {
            if (!isPortUsed(portTmp)) {
                return portTmp;
            } else {
                portTmp--;
            }
        }
        throw new RuntimeException("no available port.");
    }

    /**
     * check port used
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isPortUsed(int port) {
        boolean used = false;
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);
            used = false;
        } catch (IOException e) {
            log.info("port: {} is in use.", port);
            used = true;
        } finally {
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    log.info("IOException: {}", e.getMessage());
                }
            }
        }
        return used;
    }

}
