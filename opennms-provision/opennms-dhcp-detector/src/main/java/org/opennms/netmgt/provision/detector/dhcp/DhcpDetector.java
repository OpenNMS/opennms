package org.opennms.netmgt.provision.detector.dhcp;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetAddress;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.provision.support.dhcp.Dhcpd;


public class DhcpDetector {
    
	private static final int DEFAULT_RETRY = 0;
    private static final int DEFAULT_TIMEOUT = 3000;

    //extends BasicDetector<Request, Response>
    
    public boolean isProtocolSupported(InetAddress host) {
        return isServer(host, DEFAULT_RETRY, DEFAULT_TIMEOUT);
    }
    
    /**
     * @param host
     * @param defaultRetry
     * @param defaultTimeout
     * @return
     */
    private boolean isServer(InetAddress host, int defaultRetry, int defaultTimeout) {
     // Load the category for logging
        //
        Category log = ThreadCategory.getInstance(getClass());

        boolean isAServer = false;
        long responseTime = -1;

        try {
            // Dhcpd.isServer() returns the response time in milliseconds
            // if the remote host is a DHCP server or -1 if the remote
            // host is not a DHCP server.
            responseTime = Dhcpd.isServer(host, defaultTimeout, defaultTimeout);
        } catch (InterruptedIOException ioE) {
            if (log.isDebugEnabled()) {
                ioE.fillInStackTrace();
                log.debug("isServer: The DHCP discovery operation was interrupted", ioE);
            }
            ioE.printStackTrace();
        } catch (IOException ioE) {
            log.warn("isServer: An I/O exception occured during DHCP discovery", ioE);
            isAServer = false;
            ioE.printStackTrace();
        } catch (Throwable t) {
            log.error("isServer: An undeclared throwable exception was caught during test", t);
            isAServer = false;
            t.printStackTrace();
        }

        // If response time is equal to or greater than zero
        // the remote host IS a DHCP server.
        if (responseTime >= 0)
            isAServer = true;

        // return the success/failure of this
        // attempt to contact a DHCP server.
        //
        return isAServer;
    }
}