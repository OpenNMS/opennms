/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/
package org.opennms.smoketest.utils;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.Callable;

/**
 * Utilities for testing network connectivity.
 *
 * @author jwhite
 */
public class NetUtils {

    public static boolean isTcpPortOpen(int port) {
        return isTcpPortOpen(new InetSocketAddress("127.0.0.1", port));
    }

    public static boolean isTcpPortOpen(InetSocketAddress addr) {
        try (Socket socket = new Socket()) {
            socket.connect(addr, 100);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public static Callable<Boolean> isTcpPortOpenCallable(final int port) {
        return new Callable<Boolean>() {
            public Boolean call() throws Exception {
                return isTcpPortOpen(port);
            }
        };
    }
}
