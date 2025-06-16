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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.netmgt.config.NotificationManager;
import org.opennms.netmgt.model.notifd.Argument;
import org.opennms.netmgt.model.notifd.NotificationStrategy;


/**
 * Implements NotificationStragey pattern used to send notifications using the
 * XMPP message protocol
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:jonathan@opennms.org">Jonathan Sartin</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:jonathan@opennms.org">Jonathan Sartin</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:jonathan@opennms.org">Jonathan Sartin</a>
 * @version $Id: $
 */
public class XMPPGroupNotificationStrategy implements NotificationStrategy {
    private static final Logger LOG = LoggerFactory.getLogger(XMPPGroupNotificationStrategy.class);

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
	 * <p>Constructor for XMPPGroupNotificationStrategy.</p>
	 */
	public XMPPGroupNotificationStrategy() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opennms.netmgt.notifd.NotificationStrategy#send(java.util.List)
	 */
	/** {@inheritDoc} */
        @Override
	public int send(List<Argument> arguments) {

		try {

			String[] parsedArgs = this.parseArguments(arguments);

			XMPPNotificationManager xmppManager = XMPPNotificationManager.getInstance();

			xmppManager.sendGroupChat(parsedArgs[XMPP_TO],parsedArgs[XMPP_MESSAGE]);

		} catch (Throwable e) {
			LOG.error(e.getMessage());
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
