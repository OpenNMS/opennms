/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.plugins.dbnotifier.test.manual;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
			
			List<String> paramList = new ArrayList<>();
			
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
