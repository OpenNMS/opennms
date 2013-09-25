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
