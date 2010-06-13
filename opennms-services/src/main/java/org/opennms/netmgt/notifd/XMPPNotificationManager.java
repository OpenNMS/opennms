//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006-2008 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//

package org.opennms.netmgt.notifd;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.ConfigFileConstants;

/**
 * Singleton class used to send messages to an XMPP Server. Used by
 * XMPPNotificationStragetgy and XMPPGroupNotificationStrategy
 * 
 * @author <a href="mailto:jonathan@opennms.org">Jonathan Sartin</a>
 * @author <a href="mailto:ranger@opennms.org">Benjamin Reed</a>
 */
public class XMPPNotificationManager {

	private final Properties props = new Properties();

	private static final String LOG4J_CATEGORY = "OpenNMS.Notifd";

	private static final String XMPP_RESOURCE = "notifd";

	private static final String TRUST_STORE_PASSWORD = "changeit";

	private static final String XMPP_PORT = "5222";

	private final XMPPConnection xmpp;

	private final ConnectionConfiguration xmppConfig; 

	private final String xmppServer;

	private final String xmppServiceName;
	
	private final String xmppUser;

	private final String xmppPassword;

	private final int xmppPort;

	private final HashMap<String, MultiUserChat> rooms = new HashMap<String, MultiUserChat>();

	private static XMPPNotificationManager instance = null;

	private ConnectionListener conlistener = new ConnectionListener() {
		public void connectionClosed() {
			log().debug("XMPP connection closed");
		}

		public void connectionClosedOnError(Exception e) {
			log().warn("XMPP connection closed", e);
		}

        public void reconnectingIn(int seconds) {
            if (log().isDebugEnabled()) log().debug("XMPP reconnecting in " + seconds + " seconds");
        }

        public void reconnectionFailed(Exception e) {
            log().warn("XMPP reconnection failed", e);
            xmpp.disconnect();
            instance = null;
            
        }

        public void reconnectionSuccessful() {
            log().debug("XMPP reconnection succeeded");
        }
	};

	protected XMPPNotificationManager() {

		// get the category logger
		String oldPrefix = ThreadCategory.getPrefix();
		ThreadCategory.setPrefix(LOG4J_CATEGORY);

		try {
			// Load up some properties

			File config = null;
			try {
				config = ConfigFileConstants.getFile(ConfigFileConstants.XMPP_CONFIG_FILE_NAME);
			} catch (IOException e) {
				log().warn(ConfigFileConstants.XMPP_CONFIG_FILE_NAME + " not readable", e);
			}
			if (Boolean.getBoolean("useSystemXMPPConfig") || !config.canRead()) {
				this.props.putAll(System.getProperties());
			} else {
				FileInputStream fis = null;
				try {
					fis = new FileInputStream(config);
					this.props.load(fis);
				} catch (FileNotFoundException e) {
					log().warn("unable to load " + config, e);
				} catch (IOException e) {
					log().warn("unable to load " + config, e);
				} finally {
					IOUtils.closeQuietly(fis);
				}
			}

			xmppServer = this.props.getProperty("xmpp.server");
			xmppServiceName = this.props.getProperty("xmpp.servicename", xmppServer);
			xmppUser = this.props.getProperty("xmpp.user");
			xmppPassword = this.props.getProperty("xmpp.pass");
			xmppPort = Integer.valueOf(this.props.getProperty("xmpp.port", XMPP_PORT));

			xmppConfig = new ConnectionConfiguration(xmppServer, xmppPort, xmppServiceName);

			boolean debuggerEnabled = Boolean.parseBoolean(props.getProperty("xmpp.debuggerEnabled"));
			xmppConfig.setDebuggerEnabled(debuggerEnabled);
			if (debuggerEnabled) {
				log().setLevel(ThreadCategory.Level.DEBUG);
			}

			xmppConfig.setSASLAuthenticationEnabled(Boolean.parseBoolean(props.getProperty("xmpp.SASLEnabled", "true")));
			xmppConfig.setSelfSignedCertificateEnabled(Boolean.parseBoolean(props.getProperty("xmpp.selfSignedCertificateEnabled")));

			if (Boolean.parseBoolean(props.getProperty("xmpp.TLSEnabled"))) {
				xmppConfig.setSecurityMode(SecurityMode.enabled);
			} else {
				xmppConfig.setSecurityMode(SecurityMode.disabled);
			}
			if (this.props.containsKey("xmpp.truststorePassword")) {
				xmppConfig.setTruststorePassword(this.props.getProperty("xmpp.truststorePassword"));
			} else {
				xmppConfig.setTruststorePassword(TRUST_STORE_PASSWORD);
			}

			if (log().isDebugEnabled()) {
				log().debug("XMPP Manager connection config: " + xmppConfig.toString());
			}

			xmpp = new XMPPConnection(xmppConfig);

			// Connect to xmpp server
			connectToServer();
		} finally {
			ThreadCategory.setPrefix(oldPrefix);
		}
	}

	private void connectToServer() {
		try {
			log().debug("Attempting vanilla XMPP Connection to " + xmppServer + ":" + xmppPort);
			xmpp.connect();
			if (xmpp.isConnected()) {
				log().debug("XMPP Manager successfully connected");
				// Following requires a later version of the library
				if (xmpp.isSecureConnection()) 
					log().debug("XMPP Manager successfully nogotiated a secure connection");
				if (xmpp.isUsingTLS()) 
					log().debug("XMPP Manager successfully nogotiated a TLS connection");
				log().debug("XMPP Manager Connected"); 
				login();
				// Add connection listener
				xmpp.addConnectionListener(conlistener);
			} else {
				log().debug("XMPP Manager Not Connected");
			}
		} catch (Exception e) {
			log().fatal("XMPP Manager unable to connect", e);
		}
	}

    /**
     * Check if manager is logged in to xmpp server.
     * 
     * @return true if logged in, false otherwise
     */

    private void login() {
        try {
            if (xmpp.isConnected()) {
                log().debug("XMPP Manager logging in");
                xmpp.login(xmppUser, xmppPassword, XMPP_RESOURCE);
                rooms.clear();
            } else {
                log().debug("XMPP Manager unable to login: Not connected to XMPP server");
            }
        } catch (Exception e) {
            log().fatal("XMPP Manager unable to login: ", e);
        }
    }

	/**
	 * get an instance of the XMPPNotificationManager
	 * 
	 * @return instance of XMPPNotificationManager
	 */

	public static synchronized XMPPNotificationManager getInstance() {

		if (instance == null) {
			instance = new XMPPNotificationManager();
		}

		return instance;

	}

	public boolean isLoggedIn() {
		return (xmpp.isAuthenticated());
	}

	/**
	 * send an xmpp message to a specified recipient.
	 * 
	 * @param xmppTo
	 *            recipient of the xmpp message
	 * @param xmppMessage
	 *            text to be sent in the body of the message
	 * @return true if message is sent, false otherwise
	 */

	private static class NullMessageListener implements MessageListener {
        public void processMessage(Chat chat, Message message) {
        }
	}
	public boolean sendMessage(String xmppTo, String xmppMessage) {
	    if (!isLoggedIn()) {
	        connectToServer();
	    }
		try {
		    ChatManager cm = xmpp.getChatManager();
			cm.createChat(xmppTo, new NullMessageListener()).sendMessage(xmppMessage);
			log().debug("XMPP Manager sent message to: " + xmppTo);
		} catch (XMPPException e) {
			log().fatal("XMPP Exception Sending message ", e);
			return false;
		}

		return true;

	}

	/**
	 * send an xmpp message to a specified Chat Room.
	 * 
	 * @param xmppChatRoom
	 *            room to send message to.
	 * @param xmppMessage
	 *            text to be sent in the body of the message
	 * @return true if message is sent, false otherwise
	 */
	public boolean sendGroupChat(String xmppChatRoom, String xmppMessage) {

		MultiUserChat groupChat;

		if (rooms.containsKey(xmppChatRoom)) {
			groupChat = rooms.get(xmppChatRoom);
		} else {
			log().debug("Adding room: " + xmppChatRoom);
			groupChat = new MultiUserChat(xmpp, xmppChatRoom);
			rooms.put(xmppChatRoom, groupChat);
		}

		if (!groupChat.isJoined()) {
			log().debug("Joining room: " + xmppChatRoom);
			try {
				groupChat.join(xmppUser);
			} catch (XMPPException e) {
				log().fatal("XMPP Exception joining chat room ", e);
				return false;
			}
		}

		try {
			groupChat.sendMessage(xmppMessage);
			log().debug("XMPP Manager sent message to: " + xmppChatRoom);
		} catch (XMPPException e) {
			log().fatal("XMPP Exception sending message to Chat room", e);
			return false;
		}

		return true;

	}
	
	protected ThreadCategory log() {
		return ThreadCategory.getInstance(this.getClass());
	}
}
