package org.opennms.netmgt.protocols.ssh;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.NoRouteToHostException;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.protocols.InsufficientParametersException;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class Ssh {
    // 30 second timeout
    public static final Integer DEFAULT_TIMEOUT = 30000;
    
    // SSH port is 22
    public static final Integer DEFAULT_PORT = 22;
    
    JSch m_jsch = new JSch();
    private Session m_session;
    private Throwable m_exception;
    private String m_serverVersion;
    private InetAddress m_address;
    private Integer m_port = DEFAULT_PORT;
    private Integer m_timeout = DEFAULT_TIMEOUT;
    
    public void setAddress(InetAddress address) {
        m_address = address;
    }
    
    public InetAddress getAddress() {
        return m_address;
    }
    
    public void setPort(int port) {
        m_port = port;
    }
    
    public Integer getPort() {
        return m_port;
    }
    
    public void setTimeout(int milliseconds) {
        m_timeout = milliseconds;
    }

    public Integer getTimeout() {
        return m_timeout;
    }
    
    public String getServerVersion() {
        return m_serverVersion;
    }

    protected Session getSession() {
        return m_session;
    }
    
    /**
     * Attempt to connect, based on the parameters which have been set in
     * the object.
     * 
     * @return true if it is able to connect
     * @throws InsufficientParametersException
     */
    protected boolean connect() throws InsufficientParametersException {
        if (getAddress() == null) {
            throw new InsufficientParametersException("you must specify an address");
        }
        if (getPort() == null) {
            throw new InsufficientParametersException("you must specify a port");
        }
        if (getTimeout() == null) {
            throw new InsufficientParametersException("you must specify a timeout");
        }
        
        m_exception = null;
        m_serverVersion = null;
        m_session = null;
        try {
            m_session = m_jsch.getSession("opennms", getAddress().getHostAddress(), getPort());
            m_session.connect(getTimeout());
            m_serverVersion = m_session.getServerVersion();
            return true;
        } catch (JSchException e) {
            m_exception = e;
            if (e.getCause() != null) {
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
                // ugh, string parse, maybe we can get him to fix this in a newer jsch release
                if (e.getMessage().matches("^.*(socket is not established|connection is closed by foreign host|java.io.(IOException|InterruptedIOException)|java.net.(ConnectException|NoRouteToHostException)).*$")) {
                    log().debug("did not connect enough to verify SSH server", e);
                    return false;
                }
            }
            if (m_session != null) {
                if (m_session.isConnected()) {
                    m_serverVersion = m_session.getServerVersion();
                }
            }
            log().debug("valid SSH server is listening: " + e.getMessage());
            return true;
        }
    }

    protected Throwable getError() {
        return m_exception;
    }

    private Category log() {
        return ThreadCategory.getInstance(getClass());
    }
}
