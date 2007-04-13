/*
 * Modifications:
 *
 * 2007 Apr 13: Genericize List passed to send method. - dj@opennms.org
 * 2005 Mar 07: Created this file.
 *
 * Copyright (C) 2005, The OpenNMS Group, Inc..
 */

package org.opennms.netmgt.notifd;

import java.util.List;

import org.opennms.core.utils.Argument;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.NotificationManager;


/**
 * Implements NotificationStragey pattern used to send notifications using the
 * XMPP message protocol.
 * 
 * @author <A HREF="mailto:opennms@obado.net">Chris Abernethy </A>
 */
public class XMPPNotificationStrategy implements NotificationStrategy {

	/**
	 * String used to identify the user to whom the XMPP message will be sent.
	 */
	private static final int XMPP_TO;

	/**
	 * Text of XMPP Message to be sent.
	 */
	private static final int XMPP_MESSAGE;

	/**
	 * The value of this constant indicates the number of XMPP constants
	 * defined.
	 */
	private static final int XMPP_MAX;

	/**
	 * Mapping of index values to meaningful strings.
	 */
	private static final String[] INDEX_TO_NAME;

	// Initialize constant class data

	static {

		XMPP_TO = 0;
		XMPP_MESSAGE = 1;
		XMPP_MAX = 2;

		INDEX_TO_NAME = new String[XMPP_MAX];

		INDEX_TO_NAME[XMPP_TO] = "To";
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
	public int send(List<Argument> arguments) {

		try {

			String[] parsedArgs = this.parseArguments(arguments);

			XMPPNotificationManager xmppManager = XMPPNotificationManager.getInstance();

			xmppManager.sendMessage(parsedArgs[XMPP_TO],parsedArgs[XMPP_MESSAGE]);

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

	private String[] parseArguments(List<Argument> arguments) throws Exception {

		String[] parsedArgs = new String[XMPP_MAX];

		for (int i = 0; i < arguments.size(); i++) {

			Argument arg = arguments.get(i);

			if (NotificationManager.PARAM_XMPP_ADDRESS.equals(arg.getSwitch())) {
				parsedArgs[XMPP_TO] = arg.getValue();
			} else if (NotificationManager.PARAM_TEXT_MSG.equals(arg
					.getSwitch())) {
				parsedArgs[XMPP_MESSAGE] = arg.getValue();
			}

		}

		for (int i = 0; i < XMPP_MAX; ++i) {
			if (parsedArgs[i] == null) {
				throw (new Exception(
						"Incomplete argument set, missing argument: "
								+ INDEX_TO_NAME[i]));
			}
		}

		return parsedArgs;

	}

}
