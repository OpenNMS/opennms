/*
 * Created on Mar 7, 2005
 * Copyright (C) 2005, The OpenNMS Group, Inc..
 * 
 */

package org.opennms.netmgt.notifd;

import java.io.FileInputStream;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Category;
import org.opennms.core.utils.Argument;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.ConfigFileConstants;
import org.opennms.netmgt.config.NotificationManager;

import org.jivesoftware.smack.XMPPConnection;

/**
 * Implements NotificationStragey pattern used to send notifications using the
 * XMPP message protocol.
 * 
 * @author <A HREF="mailto:opennms@obado.net">Chris Abernethy </A>
 * 
 */
public class XMPPNotificationStrategy implements NotificationStrategy {

	/**
	 * String used to identify the user to whom the XMPP
	 * message will be sent.
	 */
	private static final int XMPP_TO;

	/**
	 * Text of XMPP Message to be sent.
	 */
	private static final int XMPP_MESSAGE;

	/**
	 * The value of this constant indicates the number of
	 * XMPP constants defined.
	 */
	private static final int XMPP_MAX;
	
	/**
	 * Mapping of index values to meaningful strings.
	 */
	private static final String[] INDEX_TO_NAME;

    private Properties props = new Properties();
	private Category log     = null;

	// Initialize constant class data

	static {

    	XMPP_TO      = 0;
    	XMPP_MESSAGE = 1;
    	XMPP_MAX     = 2;

    	INDEX_TO_NAME = new String[XMPP_MAX];

    	INDEX_TO_NAME[XMPP_TO]      = "To";
    	INDEX_TO_NAME[XMPP_MESSAGE] = "Message";
    	
    }

    /**
     * 
     */
    public XMPPNotificationStrategy() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opennms.netmgt.notifd.NotificationStrategy#send(java.util.List)
     */
    public int send(List arguments) {

        try {

        	this.props.load(new FileInputStream(ConfigFileConstants.getFile(ConfigFileConstants.XMPP_CONFIG_FILE_NAME)));

        	String[] parsedArgs = this.parseArguments(arguments);

        	String server = this.props.getProperty("xmpp.server");
            String user   = this.props.getProperty("xmpp.user");
            String pass   = this.props.getProperty("xmpp.pass");

            XMPPConnection connection = new XMPPConnection(server);

            connection.login(user, pass);
            connection.createChat(parsedArgs[XMPP_TO]).sendMessage(parsedArgs[XMPP_MESSAGE]);

        } catch (Exception e) {
        	ThreadCategory.getInstance(getClass()).error(e.getMessage());
        	return 1;
        }

        return 0;

    }

    /**
     * This method extracts the xmpp address and message text from the
     * parameters passed in the notification.
     * 
     * @param arguments
     * @return String[]
     * @throws Exception
     */

    private String[] parseArguments(List arguments) throws Exception {

    	String[] parsedArgs = new String[XMPP_MAX];

    	for (int i = 0; i < arguments.size(); i++) {

            Argument arg = (Argument) arguments.get(i);

            if (NotificationManager.PARAM_XMPP_ADDRESS.equals(arg.getSwitch())) {
                parsedArgs[XMPP_TO] = arg.getValue();
            } else if (NotificationManager.PARAM_TEXT_MSG.equals(arg.getSwitch())) {
                parsedArgs[XMPP_MESSAGE] = arg.getValue();
            }

    	}

    	for (int i = 0; i < XMPP_MAX; ++i) {
    		if (parsedArgs[i] == null) {
    			throw( new Exception("Incomplete argument set, missing argument: " + INDEX_TO_NAME[i] ) );
    		}
    	}
    	
    	return parsedArgs;
    	
    }

}
