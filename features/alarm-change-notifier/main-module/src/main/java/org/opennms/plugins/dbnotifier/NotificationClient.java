package org.opennms.plugins.dbnotifier;


/**
 * Client interface used to register to receive DbNotifications
 * @author admin
 *
 */
public interface NotificationClient {

	public void sendDbNotification(DbNotification dbNotification);

	public void init();
	
	public void destroy();
	
}
