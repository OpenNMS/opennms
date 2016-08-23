package org.opennms.plugins.dbnotifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class simply prints out the received notification. Used primarily for testing.
 * @author admin
 *
 */
public class VerySimpleNotificationClient implements NotificationClient {
	private static 	final Logger LOG = LoggerFactory.getLogger(DbNotificationClientQueueImpl.class);
	
	@Override
	public void sendDbNotification(DbNotification dbNotification) {
		if(LOG.isDebugEnabled()) LOG.debug("Notification received by VerySimpleNotificationClient :\n processId:"+dbNotification.getProcessId()
				+ "\n channelName:"+dbNotification.getChannelName()
				+ "\n payload:"+dbNotification.getPayload());

	}


	@Override
	public void init() {
		// TODO Auto-generated method stub

	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

}
