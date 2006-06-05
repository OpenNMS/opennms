/**
 * 
 */
package org.opennms.netmgt.notifd;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Properties;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.GroupChat;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.ConfigFileConstants;
import org.apache.log4j.Category;
import org.apache.log4j.Priority;


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
	
	private static XMPPConnection xmpp = null;
	
	private Category log = null;

	private String xmppServer;

	private String xmppUser;

	private String xmppPassword;
	
	private HashMap rooms;

	private static XMPPNotificationManager instance = null;

	private ConnectionListener conlistener = new ConnectionListener() {
		public void connectionClosed() {
			if (log.isEnabledFor(Priority.DEBUG))
				log.debug("XMPP Connection Closed");
			// null the group you're joined and reconnect
			instance = null;
		}

		public void connectionClosedOnError(Exception e) {
			if (log.isEnabledFor(Priority.DEBUG))
				log.debug("XMPP Connection Closed" + e.toString());
			// null the group you're joined and reconnect
			instance = null;
		}
	};

	protected XMPPNotificationManager() {

		ThreadCategory.setPrefix(LOG4J_CATEGORY);
		log = ThreadCategory.getInstance(this.getClass());

		try {
			this.props.load(new FileInputStream(ConfigFileConstants
					.getFile(ConfigFileConstants.XMPP_CONFIG_FILE_NAME)));
			xmppServer = this.props.getProperty("xmpp.server");
			xmppUser = this.props.getProperty("xmpp.user");
			xmppPassword = this.props.getProperty("xmpp.pass");
			} catch (Exception e) {
			log.error(e.getMessage());
		}

		// Connect to xmpp server
		
		connect();
		
		// Add connection listener to reconnect if server disappears
		
		xmpp.addConnectionListener(conlistener);
		

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

	private void connect() {
		try {
			xmpp = new XMPPConnection(xmppServer);
			if (xmpp.isConnected()) {
				if (log.isEnabledFor(Priority.DEBUG))
					log.debug("XMPP Manager Connected");
				xmpp.login(xmppUser,xmppPassword,XMPP_RESOURCE);
				rooms = new HashMap();
			} else if (log.isEnabledFor(Priority.FATAL))
				log.debug("XMPP Manager Not Connected");
		} catch (Exception e) {
			if (log.isEnabledFor(Priority.FATAL))
				log.fatal("XMPP Manager unable to connect: ", e);
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
		} catch (XMPPException e) {
			if (log.isEnabledFor(Priority.FATAL))
				log.fatal("XMPP Exception Sending message ", e);
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
		
		if (rooms.containsKey(xmppChatRoom)){
			groupChat = (GroupChat)rooms.get(xmppChatRoom);
		} else {
			if (log.isEnabledFor(Priority.DEBUG))
				log.debug("Adding room: " + xmppChatRoom);
			groupChat = xmpp.createGroupChat(xmppChatRoom);
			rooms.put(room, groupChat);
		} 
		
		if (!groupChat.isJoined()) {
			if (log.isEnabledFor(Priority.DEBUG))
				log.debug("Joining room: " + xmppChatRoom);
			try {
				groupChat.join(xmppUser);
			} catch (XMPPException e) {
				if (log.isEnabledFor(Priority.FATAL))
					log.fatal("XMPP Exception joining chat room ", e);
					return false;
			}
		}
				
		try {
			groupChat.sendMessage(xmppMessage);
		} catch (XMPPException e) {
			if (log.isEnabledFor(Priority.FATAL))
				log.fatal("XMPP Exception sending message to Chat room", e);
			return false;
		}
		
		return true;
					
	}
}
