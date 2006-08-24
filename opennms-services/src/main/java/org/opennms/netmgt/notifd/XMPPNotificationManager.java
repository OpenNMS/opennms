/**
 * 
 */
package org.opennms.netmgt.notifd;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Properties;

import org.apache.log4j.Category;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.GroupChat;
import org.jivesoftware.smack.SSLXMPPConnection;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.ConfigFileConstants;

/**
 * Singleton class used to send messages to an XMPP Server. Used by
 * XMPPNotificationStragetgy and XMPPGroupNotificationStrategy
 * 
 * @author <A HREF="mailto:jonathan@opennms.org">Jonathan Sartin</A>
 */

public class XMPPNotificationManager {

	private Properties props = new Properties();

	private static final String LOG4J_CATEGORY = "OpenNMS.Notifd";

	private static final String XMPP_RESOURCE = "notifd";

	private static final String TRUST_STORE_PASSWORD = "changeit";

	private static final int XMPP_PORT = 5222;

	private static XMPPConnection xmpp = null;

	private static ConnectionConfiguration xmppConfig = null; 

	private String xmppServer;

	private String xmppUser;

	private String xmppPassword;

	private int xmppPort;

	private HashMap rooms;

	private static XMPPNotificationManager instance = null;

	private ConnectionListener conlistener = new ConnectionListener() {
		public void connectionClosed() {
			log().debug("XMPP Connection Closed");
			// null the group you're joined and reconnect
			instance = null;
		}

		public void connectionClosedOnError(Exception e) {
			log().debug("XMPP Connection Closed" + e.toString());
			// null the group you're joined and reconnect
			instance = null;
		}
	};

	protected XMPPNotificationManager() {

		// get the category logger
		
		ThreadCategory.setPrefix(LOG4J_CATEGORY);
		
		// Load up some properties

		try {
			this.props.load(new FileInputStream(ConfigFileConstants
					.getFile(ConfigFileConstants.XMPP_CONFIG_FILE_NAME)));
			xmppServer = this.props.getProperty("xmpp.server");
			xmppUser = this.props.getProperty("xmpp.user");
			xmppPassword = this.props.getProperty("xmpp.pass");
			if (this.props.containsKey("xmpp.port")) {
				xmppPort = Integer.valueOf(this.props.getProperty("xmpp.port")).intValue();
			} else {
				xmppPort = XMPP_PORT;
			}
		        xmppConfig = new ConnectionConfiguration(xmppServer, xmppPort);
			if (this.props.containsKey("xmpp.TLSEnabled")
					&& this.props.getProperty("xmpp.TLSEnabled").equals("true")) {
				xmppConfig.setTLSEnabled(true);
				log().debug("XMPPManager Enabling TLS");
			} else {
				xmppConfig.setTLSEnabled(false);
			}
			if (this.props.containsKey("xmpp.selfSignedCertificateEnabled")
					&& this.props.getProperty("xmpp.selfSignedCertificateEnabled").equals("true")) {
				xmppConfig.setSelfSignedCertificateEnabled(true);
				log().debug("XMPPManager Enabling self-signed certificates");
			} else {
				xmppConfig.setSelfSignedCertificateEnabled(false);
			}
			if (this.props.containsKey("xmpp.truststorePassword")) {
				xmppConfig.setTruststorePassword(this.props.getProperty("xmpp.truststorePassword"));
				log().debug("XMPPManager set non-default truststore password");
			} else {
				log().debug("XMPPManager set default truststore password");
				xmppConfig.setTruststorePassword(TRUST_STORE_PASSWORD);
			}
			if (this.props.containsKey("xmpp.debuggerEnabled") 
					&& this.props.getProperty("xmpp.debuggerEnabled").equals("true")) {
				xmppConfig.setDebuggerEnabled(true);
				log().debug("XMPPManager set debugger enabled");
			} else {
				xmppConfig.setDebuggerEnabled(false);
				log().debug("XMPPManager set debugger disabled");
			}
			xmppConfig.setSASLAuthenticationEnabled(true);
			log().debug("XMPP Manager connection config: " + xmppConfig.toString());
			log().debug("never get here");
		} catch (Exception e) {
			log().error("XMPP Manager couldn't configure connection : ", e);
		}

		// dump some settings:



		// Connect to xmpp server

		try {
			log().debug("Attempting vanilla XMPP Connection to " + xmppServer + ":" + xmppPort);
			xmpp = new XMPPConnection(xmppConfig);
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
			log().fatal("XMPP Manager unable to connect : ", e);
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

	private void login() {
		try {
			if (xmpp.isConnected()) {
				log().debug("XMPP Manager logging in");
				xmpp.login(xmppUser, xmppPassword, XMPP_RESOURCE);
				rooms = new HashMap();
			} else
				log().debug("XMPP Manager unable to login: Not connected to XMPP server");
		} catch (Exception e) {
			log().fatal("XMPP Manager unable to login: ", e);
		}
	}

	/**
	 * Check if manager is logged in to xmpp server.
	 * 
	 * @return true if logged in, false otherwise
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

	public boolean sendMessage(String xmppTo, String xmppMessage) {

		try {
			xmpp.createChat(xmppTo).sendMessage(xmppMessage);
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

		GroupChat groupChat;

		String room = new String(xmppChatRoom);

		if (rooms.containsKey(xmppChatRoom)) {
			groupChat = (GroupChat) rooms.get(xmppChatRoom);
		} else {
			log().debug("Adding room: " + xmppChatRoom);
			groupChat = xmpp.createGroupChat(xmppChatRoom);
			rooms.put(room, groupChat);
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
	
	protected Category log() {
    	return ThreadCategory.getInstance();
    }
}
