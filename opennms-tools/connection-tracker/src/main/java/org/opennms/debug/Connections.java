/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.debug;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.sql.PooledConnection;

public class Connections {
    private static Map<PooledConnection, ConnectionInfo> m_pooledConnections = Collections.synchronizedMap(new HashMap<PooledConnection,ConnectionInfo>());
    private static final Timer m_timer = new Timer();
    private static boolean SHOW_TRACK_AND_COMPLETE = Boolean.getBoolean("org.opennms.debug.showTrackAndComplete");

    static {
        m_timer.schedule(new TimerTask() {
            @Override public void run() {
                Connections.printPooledStatus();
            }}, 0, 60 * 1000);
    }


    public static void track(final PooledConnection c) {
        if (SHOW_TRACK_AND_COMPLETE) System.err.println("+ " + c);
        m_pooledConnections.put(c, new ConnectionInfo());
    }

    public static void complete(final PooledConnection c) {
        if (SHOW_TRACK_AND_COMPLETE) System.err.println("- " + c);
        m_pooledConnections.remove(c);
    }

    public static void printPooledStatus() {
        synchronized (m_pooledConnections) {
            System.err.println("===========================================================");
            System.err.println("Leaked Database Connection Monitor - talk to Matt & Ben if demo hangs");
            System.err.println(new Date() + ": " + m_pooledConnections.size() + " active connection(s)");
            for (final Map.Entry<PooledConnection, ConnectionInfo> entry : m_pooledConnections.entrySet()) {
                System.err.println(entry.getKey() + ": " + entry.getValue().getDate() + ":");
                entry.getValue().getException().printStackTrace();
                System.err.println();
            }
            System.err.println("===========================================================");
        }
    }

    public static int getPooledConnectionCount() {
        return m_pooledConnections.size();
    }
}
