package org.opennms.netmgt.protocols;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.NoRouteToHostException;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.model.PollStatus;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class SSH implements Protocol {
    JSch m_jsch = new JSch();
    private Throwable m_exception;
    private String m_serverVersion;
    
    private boolean connect(InetAddress address, int port, int timeout) {
        m_exception = null;
        m_serverVersion = null;
        Session session = null;
        try {
            session = m_jsch.getSession("opennms", address.getHostAddress(), port);
            session.connect(timeout);
            m_serverVersion = session.getServerVersion();
            return true;
        } catch (JSchException e) {
            m_exception = e;
            if (e.getCause() != null) {
                log().debug("got a root cause");
                Class cause = e.getCause().getClass();
                if (cause == ConnectException.class) {
                    log().debug("connection refused", e);
                    return false;
                } else if (cause == NoRouteToHostException.class) {
                    log().debug("no route to host", e);
                    return false;
                } else if (cause == InterruptedIOException.class) {
                    log().debug("connection timeout", e);
                    return false;
                } else if (cause == IOException.class) {
                    log().debug("An I/O exception occurred", e);
                    return false;
                } else if (e.getMessage().matches("^.*(connection is closed by foreign host).*$")) {
                    log().debug("JSCH failed", e);
                    return false;
                }
            } else {
                log().debug("did not get a root cause");
                // ugh, string parse, maybe we can get him to fix this in a newer jsch release
                if (e.getMessage().matches("^.*(java.io.(IOException|InterruptedIOException)|java.net.(ConnectException|NoRouteToHostException)).*$")) {
                    log().debug("did not connect enough to verify SSH server", e);
                    return false;
                }
            }
            if (session != null) {
                m_serverVersion = session.getServerVersion();
            }
            log().debug("passed exception: " + e.getMessage());
            return true;
        }
    }

    public String getServerVersion() {
        return m_serverVersion;
    }

    public boolean exists(InetAddress address, int port, int timeout) {
        return connect(address, port, timeout);
    }

    public double check(InetAddress address, int port, int timeout) throws FailedCheckException {
        long nanoStartTime = System.nanoTime();
        boolean isAvailable = connect(address, port, timeout);
        long nanoEndTime = System.nanoTime();

        if (!isAvailable) {
            throw new FailedCheckException(m_exception);
        }

        return ((nanoEndTime - nanoStartTime) / 100000.0);
        
        /*
        if (connect(address, port, timeout)) {
            
        }
        return connect(address, port, timeout);

        PollStatus ps = PollStatus.unavailable();
        return isAvailable;
        
        if (isAvailable) {
            ps = PollStatus.available();
            ps.setProperty("response-time", (nanoStartTime - nanoEndTime) / 1000000.0);
            ps.setProperty("server-version", value)
        } else if (m_exception.getMessage().matches("java.io.InterruptedIOException")) {
            ps = PollStatus.unresponsive();
        } else {
            ps = PollStatus.unavailable();
        }
        
        return ps;
        */
    }

    private Category log() {
        return ThreadCategory.getInstance(getClass());
    }
}
