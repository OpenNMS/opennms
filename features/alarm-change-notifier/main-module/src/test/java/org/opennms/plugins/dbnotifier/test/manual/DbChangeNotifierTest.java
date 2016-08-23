package org.opennms.plugins.dbnotifier.test.manual;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import org.junit.Test;

import org.opennms.plugins.dbnotifier.DatabaseChangeNotifier;
import org.opennms.plugins.dbnotifier.DbNotificationClientQueueImpl;
import org.opennms.plugins.dbnotifier.DbNotifierDataSourceFactory;
import org.opennms.plugins.dbnotifier.NotificationClient;
import org.opennms.plugins.dbnotifier.alarmnotifier.AlarmChangeNotificationClient;


public class DbChangeNotifierTest {

	@Test
	public void test1() {
		System.out.println("Starting DbChangeNotifierTest test1");
		
		DbNotifierDataSourceFactory dsFactory = new DbNotifierDataSourceFactory();
		
		dsFactory.setDataBaseName("opennms");
		dsFactory.setUserName("opennms");
		dsFactory.setPassWord("opennms");
		dsFactory.setHostname("localhost");
		dsFactory.setPort("5432");

// TODO remove
//		PGDataSource pgDataSource = new PGDataSource();
//		
//		pgDataSource.setHost("localhost");
//		pgDataSource.setPort(5432);
//		pgDataSource.setDatabase("opennms");
//		pgDataSource.setUser("opennms");
//		pgDataSource.setPassword("opennms");

		DatabaseChangeNotifier dbChangeNotifier = null;

		try {
			System.out.println("DbChangeNotifierTest creating connection - this is quite slow");
			
			List<String> paramList = new ArrayList<String>();
			
			paramList.add(DatabaseChangeNotifier.NOTIFY_EVENT_CHANGES);
			paramList.add(DatabaseChangeNotifier.NOTIFY_ALARM_CHANGES);
			
			dbChangeNotifier = new DatabaseChangeNotifier(dsFactory,paramList);
			
			DbNotificationClientQueueImpl dbNotificationQueueClient= new DbNotificationClientQueueImpl();
			
			Map<String, NotificationClient> channelHandlingClients= new HashMap<String, NotificationClient>();
//			channelHandlingClients.put("opennms_alarm_changes", new VerySimpleNotificationClient());
			
			AlarmChangeNotificationClient alarmChangeNotificationClient = new AlarmChangeNotificationClient();
			channelHandlingClients.put("opennms_alarm_changes", alarmChangeNotificationClient);
			
			dbNotificationQueueClient.setChannelHandlingClients(channelHandlingClients);
			
			
			dbNotificationQueueClient.setDatabaseChangeNotifier(dbChangeNotifier);
			dbNotificationQueueClient.init();
			
			System.out.println("DbChangeNotifierTest initialising connection");
			dbChangeNotifier.init();
			
			System.out.println("DbChangeNotifierTest waiting for messages or until timeout");

			try{ // wait for interrupt or time out
				Thread.sleep(50000);
			} catch (InterruptedException e){}
			
			System.out.println("DbChangeNotifierTest shutting down");

		} catch (Throwable e) {
			e.printStackTrace();
		} finally {
			System.out.println("DbChangeNotifierTest destroying connection");
			try {
				if (dbChangeNotifier != null) {
					dbChangeNotifier.destroy();
				}
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}

		System.out.println("End of DbChangeNotifierTest test1");
	}

}
