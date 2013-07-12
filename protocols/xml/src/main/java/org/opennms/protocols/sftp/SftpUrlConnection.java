/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.protocols.sftp;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;


import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

/**
 * The class for managing SFTP URL Connection.
 * <p>The default connection timeout is 30 seconds.</p>
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class SftpUrlConnection extends URLConnection {

    /** The Constant default timeout in milliseconds. */
    public static final int DEFAULT_TIMEOUT = 30000;

    /** The SSH session. */
    private Session m_session; 

    /** The SFTP channel. */
    private ChannelSftp m_channel;

    /** The connection flag, true when the connection has been started. */
    private boolean m_connected = false;

    /**
     * Instantiates a new SFTP URL connection.
     *
     * @param url the URL
     */
    protected SftpUrlConnection(URL url) {
        super(url);
    }

    /* (non-Javadoc)
     * @see java.net.URLConnection#connect()
     */
    @Override
    public void connect() throws IOException {
        if (m_connected) {
            return;
        }
        m_connected = true;
        if (url.getUserInfo() == null) {
            throw new IOException("User credentials required.");
        }
        JSch jsch = new JSch();
        try {
            // TODO: Experimental authentication handling using Private/Public keys
            // FIXME: We can include this property on the request object, for example:
            // <request>
            //   <parameter name='sftp.private-key.location' value='/opt/opennms/etc/private.key'/>
            // </request>
            // http://wiki.jsch.org/index.php?Manual%2FExamples%2FJschPubkeyAuthExample
            String prvkey = System.getProperty("sftp.private-key.location");
            if (prvkey != null) {
                jsch.addIdentity(prvkey);
            }
            int port = url.getPort() > 0 ? url.getPort() : url.getDefaultPort();
            String[] userInfo = url.getUserInfo().split(":");
            m_session = jsch.getSession(userInfo[0], url.getHost(), port);
            if (userInfo.length > 1) {
                m_session.setPassword(userInfo[1]);
            }
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            m_session.setConfig(config);
            m_session.setTimeout(DEFAULT_TIMEOUT);
            m_session.connect();
            m_channel = (ChannelSftp) m_session.openChannel("sftp");
            m_channel.connect();
        } catch (JSchException e) {
            disconnect();
            throw new IOException("Can't connect using " + url + " because " + e.getMessage());
        }
    }

    /**
     * Disconnect.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void disconnect() throws IOException {
        if (m_channel != null)
            m_channel.disconnect();
        if (m_session != null)
            m_session.disconnect();
    }

    /**
     * Gets the channel.
     *
     * @return the channel
     */
    public ChannelSftp getChannel() throws IOException {
        if (m_channel == null) {
            connect();
        }
        return m_channel;
    }

    /* (non-Javadoc)
     * @see java.net.URLConnection#getInputStream()
     */
    @Override
    public InputStream getInputStream() throws IOException {
        String filePath = getPath();
        try {
            return getChannel().get(filePath);
        } catch (SftpException e) {
            throw new IOException("Can't retrieve " + filePath + " from " + url.getHost() + " because " + e.getMessage());
        }
    }

    /**
     * Gets the path.
     *
     * @return the path
     * @throws SftpUrlException the SFTP URL exception
     */
    protected String getPath() throws SftpUrlException {
        return url.getPath();
    }

    /**
     * Log.
     *
     * @return the thread category
     */

}
