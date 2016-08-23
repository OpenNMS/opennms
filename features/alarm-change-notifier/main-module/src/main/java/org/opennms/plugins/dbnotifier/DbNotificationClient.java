package org.opennms.plugins.dbnotifier;

/**
 * Client interface used to register with DatabaseChangeNotifier to receive DbNotifications
 * @author admin
 *
 */
public interface DbNotificationClient extends NotificationClient{

	public void setDatabaseChangeNotifier(DatabaseChangeNotifier databaseChangeNotifier);
	
	public DatabaseChangeNotifier getDatabaseChangeNotifier();

	
}
