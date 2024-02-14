/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.notifd;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.apache.commons.io.IOUtils;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.opennms.core.logging.Logging;
import org.opennms.core.mate.api.Interpolator;
import org.opennms.core.mate.api.Scope;
import org.opennms.core.mate.api.SecureCredentialsVaultScope;
import org.opennms.core.utils.AnyServerX509TrustManager;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.features.scv.api.SecureCredentialsVault;
import org.opennms.features.scv.jceks.JCEKSSecureCredentialsVault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Singleton class used to send messages to an XMPP Server. Used by
 * XMPPNotificationStragetgy and XMPPGroupNotificationStrategy
 *
 * @author <a href="mailto:jonathan@opennms.org">Jonathan Sartin</a>
 * @author <a href="mailto:ranger@opennms.org">Benjamin Reed</a>
 * @version $Id: $
 */
public class XMPPNotificationManager {

    private static final Logger LOG = LoggerFactory.getLogger(XMPPNotificationManager.class);

	private final Properties props = new Properties();

	private static final String LOG4J_CATEGORY = "notifd";

	private static final String XMPP_RESOURCE = "notifd";

	private static final String XMPP_PORT = "5222";

	private final XMPPTCPConnection xmpp;

	private final String xmppServer;

	private final String xmppUser;

	private final String xmppPassword;

	private final int xmppPort;

	private final Map<String, MultiUserChat> rooms = new HashMap<String, MultiUserChat>();

	private static XMPPNotificationManager instance = null;

	private ConnectionListener conlistener = new ConnectionListener() {
                @Override
		public void connectionClosed() {
			LOG.debug("XMPP connection closed");
		}

                @Override
		public void connectionClosedOnError(Exception e) {
			LOG.warn("XMPP connection closed", e);
		}

                @Override
        public void reconnectingIn(int seconds) {
            LOG.debug("XMPP reconnecting in {} seconds", seconds);
        }

                @Override
        public void reconnectionFailed(Exception e) {
            LOG.warn("XMPP reconnection failed", e);
            try {
                xmpp.disconnect();
            } catch (NotConnectedException ex) {
                LOG.error("XMPP disconnect failed", ex);
            }
            instance = null;
        }

                @Override
        public void reconnectionSuccessful() {
            LOG.debug("XMPP reconnection succeeded");
        }

        @Override
        public void authenticated(XMPPConnection conn) {
            LOG.debug("XMPP authenticated");
        }

        @Override
        public void connected(XMPPConnection conn) {
            LOG.debug("XMPP connected");
        }
	};

	String getXmppPassword() {
		return xmppPassword;
	}

	String getXmppUser() {
		return xmppUser;
	}

	/**
	 * <p>Constructor for XMPPNotificationManager.</p>
	 */
	protected XMPPNotificationManager(final SecureCredentialsVault secureCredentialsVault) {
	    // mdc may be null when executing via unit tests
		Map<String,String> mdc = Logging.getCopyOfContextMap();
        try {
            if (mdc != null) {
                mdc.put(Logging.PREFIX_KEY, LOG4J_CATEGORY);
            }

			// Load up some properties
			File config = null;
			try {
				config = ConfigFileConstants.getFile(ConfigFileConstants.XMPP_CONFIG_FILE_NAME);
			} catch (IOException e) {
				LOG.warn("{} not readable", ConfigFileConstants.XMPP_CONFIG_FILE_NAME, e);
			}
			if (Boolean.getBoolean("useSystemXMPPConfig") || config == null || !config.canRead()) {
				this.props.putAll(System.getProperties());
			} else {
				FileInputStream fis = null;
				try {
					fis = new FileInputStream(config);
					this.props.load(fis);
				} catch (FileNotFoundException e) {
					LOG.warn("unable to load {}", config, e);
				} catch (IOException e) {
					LOG.warn("unable to load {}", config, e);
				} finally {
					IOUtils.closeQuietly(fis);
				}
			}

			xmppServer = this.props.getProperty("xmpp.server");
			String xmppServiceName = this.props.getProperty("xmpp.servicename", xmppServer);

			final Scope scope = new SecureCredentialsVaultScope(secureCredentialsVault);
			xmppUser = Interpolator.interpolate(this.props.getProperty("xmpp.user"), scope).output;
			xmppPassword = Interpolator.interpolate(this.props.getProperty("xmpp.pass"), scope).output;

			xmppPort = Integer.valueOf(this.props.getProperty("xmpp.port", XMPP_PORT));

			ConnectionConfiguration xmppConfig = new ConnectionConfiguration(xmppServer, xmppPort, xmppServiceName);

			boolean debuggerEnabled = Boolean.parseBoolean(props.getProperty("xmpp.debuggerEnabled"));
			xmppConfig.setDebuggerEnabled(debuggerEnabled);

			if (Boolean.parseBoolean(props.getProperty("xmpp.TLSEnabled"))) {
				xmppConfig.setSecurityMode(SecurityMode.enabled);
			} else {
				xmppConfig.setSecurityMode(SecurityMode.disabled);
			}

			if (Boolean.parseBoolean(props.getProperty("xmpp.selfSignedCertificateEnabled"))) {
	            try {
	                SSLContext ctx = SSLContext.getInstance("TLS");
	                ctx.init(null, new TrustManager[] { new AnyServerX509TrustManager() }, null);
	                xmppConfig.setCustomSSLContext(ctx);
	            } catch (NoSuchAlgorithmException | KeyManagementException e) {
	                LOG.error("Failed to create a custom SSL context", e);
	            }
			}

			// Remove all SALS mechanisms when SASL is disabled
			// There is currently no option to do this on a per-connection basis
			// so we must do it globally.
			if (!Boolean.parseBoolean(props.getProperty("xmpp.SASLEnabled", "true"))) {
			    LOG.info("Removing all SALS mechanisms from Smack.");
			    SmackConfiguration.removeSaslMechs(SmackConfiguration.getSaslMechs());
			}

			LOG.debug("XMPP Manager connection config: {}", xmppConfig);

			xmpp = new XMPPTCPConnection(xmppConfig);

			// Connect to xmpp server
			connectToServer();
		} finally {
		    if (mdc != null) {
		        Logging.setContextMap(mdc);
		    }
		}
	}

	private void connectToServer() {
		try {
			LOG.debug("Attempting vanilla XMPP Connection to {}:{}", xmppServer, xmppPort);
			xmpp.connect();
			if (xmpp.isConnected()) {
				LOG.debug("XMPP Manager successfully connected");
				// Following requires a later version of the library
				if (xmpp.isSecureConnection()) 
					LOG.debug("XMPP Manager successfully negotiated a secure connection");
				if (xmpp.isUsingTLS()) 
					LOG.debug("XMPP Manager successfully negotiated a TLS connection");
				LOG.debug("XMPP Manager Connected"); 
				login();
				// Add connection listener
				xmpp.addConnectionListener(conlistener);
			} else {
				LOG.debug("XMPP Manager Not Connected");
			}
		} catch (Throwable e) {
			LOG.error("XMPP Manager unable to connect", e);
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
                LOG.debug("XMPP Manager logging in");
                xmpp.login(xmppUser, xmppPassword, XMPP_RESOURCE);
                rooms.clear();
            } else {
                LOG.debug("XMPP Manager unable to login: Not connected to XMPP server");
            }
        } catch (Throwable e) {
            LOG.error("XMPP Manager unable to login: ", e);
        }
    }

	/**
	 * get an instance of the XMPPNotificationManager
	 *
	 * @return instance of XMPPNotificationManager
	 */
	public static synchronized XMPPNotificationManager getInstance() {
		if (instance == null) {
			instance = new XMPPNotificationManager(JCEKSSecureCredentialsVault.defaultScv());
		}
		return instance;
	}

	public static synchronized XMPPNotificationManager getInstance(final SecureCredentialsVault secureCredentialsVault) {
		if (instance == null) {
			instance = new XMPPNotificationManager(secureCredentialsVault);
		}
		return instance;
	}

	/**
	 * <p>isLoggedIn</p>
	 *
	 * @return a boolean.
	 */
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
        @Override
        public void processMessage(Chat chat, Message message) {
        }
	}
	/**
	 * <p>sendMessage</p>
	 *
	 * @param xmppTo a {@link java.lang.String} object.
	 * @param xmppMessage a {@link java.lang.String} object.
	 * @return a boolean.
	 */
	public boolean sendMessage(String xmppTo, String xmppMessage) {
	    if (!isLoggedIn()) {
	        connectToServer();
	    }
		try {
		    ChatManager cm = ChatManager.getInstanceFor(xmpp);
			cm.createChat(xmppTo, new NullMessageListener()).sendMessage(xmppMessage);
			LOG.debug("XMPP Manager sent message to: {}", xmppTo);
		} catch (XMPPException | NotConnectedException e) {
			LOG.error("XMPP Exception Sending message ", e);
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
			LOG.debug("Adding room: {}", xmppChatRoom);
			groupChat = new MultiUserChat(xmpp, xmppChatRoom);
			rooms.put(xmppChatRoom, groupChat);
		}

		if (!groupChat.isJoined()) {
			LOG.debug("Joining room: {}", xmppChatRoom);
			try {
				groupChat.join(xmppUser);
			} catch (XMPPException | NoResponseException | NotConnectedException e) {
				LOG.error("XMPP Exception joining chat room ", e);
				return false;
			}
		}

		try {
			groupChat.sendMessage(xmppMessage);
			LOG.debug("XMPP Manager sent message to: {}", xmppChatRoom);
		} catch (XMPPException | NotConnectedException e) {
			LOG.error("XMPP Exception sending message to Chat room", e);
			return false;
		}

		return true;
	}
}
