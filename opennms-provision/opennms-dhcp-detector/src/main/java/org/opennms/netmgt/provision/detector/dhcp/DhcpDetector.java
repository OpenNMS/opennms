/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 *
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 */
package org.opennms.netmgt.provision.detector.dhcp;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetAddress;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.provision.support.dhcp.Dhcpd;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
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