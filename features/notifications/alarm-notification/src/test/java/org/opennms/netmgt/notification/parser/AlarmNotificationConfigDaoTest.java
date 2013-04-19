/*******************************************************************************
 * This file is part of OpenNMS(R). Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc. OpenNMS(R) is
 * a registered trademark of The OpenNMS Group, Inc. OpenNMS(R) is free
 * software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * OpenNMS(R) is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details. You should have received a copy of the GNU General Public
 * License along with OpenNMS(R). If not, see: http://www.gnu.org/licenses/
 * For more information contact: OpenNMS(R) Licensing <license@opennms.org>
 * http://www.opennms.org/ http://www.opennms.com/
 **/
package org.opennms.netmgt.notification.parser;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

@Ignore
public class AlarmNotificationConfigDaoTest {

	@Test
	@Ignore
	public void testAlarmNotificationConfigDao() {

	}

	@Test
	@Ignore
	public void testTranslateConfigAlarmNorthbounderConfig() {

	}

	@Test
	@Ignore
	public void testGetConfig() {
		try {
			FileInputStream fstream = new FileInputStream(
					"/home/tchandra123/opennms/src/opennms/"
							+ "features/notifications/alarm-notification/src/main/etc/alarm-notification/alarmNotificationConf.xml");
			// System.err.println(xml);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String xml = "";
			String str;
			// Read File Line By Line
			while ((str = br.readLine()) != null) {
				// Print the content on the console
				xml = xml + str;
			}
			// Close the input stream
			in.close();
			System.out.println(xml);
			Resource resource = new ByteArrayResource(xml.getBytes());

			AlarmNotificationConfigDao dao = new AlarmNotificationConfigDao();
			dao.setConfigResource(resource);
			dao.afterPropertiesSet();

			AlarmNorthbounderConfig config = dao.getConfig();
			System.out.println(config);
		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
	}

	@Test
	@Ignore
	public void testaddNotification() {
		AlarmNotificationConfObjectFactory alarmNotificationConfObjectFactory = new AlarmNotificationConfObjectFactory();
		AlarmNorthbounderConfig alarmNorthbounderConfig = alarmNotificationConfObjectFactory
				.createAlarmNorthbounderConfig();
		Notification notification = alarmNotificationConfObjectFactory
				.createNotification();
		notification.setEnable(true);
		notification.setName("test");

		Script script = alarmNotificationConfObjectFactory.createScript();
		script.setScriptname("TestScript.sh");

		Errorhandling errorHandling = alarmNotificationConfObjectFactory
				.createErrorhandling();
		errorHandling.setEnable(false);
		script.setErrorhandling(errorHandling);
		notification.setScript(script);

		Ueis ueis = alarmNotificationConfObjectFactory.createUeis();
		Uei uei = alarmNotificationConfObjectFactory.createUei();
		uei.setName("uei.opennms.org/vendor/Juniper/traps/jnxVpnIfDown");

		Filter filter = alarmNotificationConfObjectFactory.createFilter();
		filter.setSeverity("Major");
		uei.getFilter().add(filter);
		ueis.getUei().add(uei);
		notification.setUeis(ueis);

		alarmNorthbounderConfig.getNotification().add(notification);
		AlarmNotificationConfigDao dao = new AlarmNotificationConfigDao();
		dao.addNotification(alarmNorthbounderConfig,
				"/home/tchandra123/chitra/Others/alarmNotificationConf.xml");
	}

	@Test
	@Ignore
	public void testDeleteNotification() {
		AlarmNorthbounderConfig alarmNorthbounderConfig = new AlarmNorthbounderConfig();
		Notification notification = new Notification();
		notification.setEnable(true);
		notification.setName("test");
		Script script = new Script();
		script.setScriptname("TestScript.sh");
		Errorhandling errorHandling = new Errorhandling();
		errorHandling.setEnable(false);
		script.setErrorhandling(errorHandling);
		notification.setScript(script);
		Ueis ueis = new Ueis();
		Uei uei = new Uei();
		uei.setName("uei.opennms.org/vendor/Juniper/traps/jnxVpnIfDown");
		Filter filter = new Filter();
		filter.setSeverity("Major");
		uei.getFilter().add(filter);
		ueis.getUei().add(uei);
		notification.setUeis(ueis);
		alarmNorthbounderConfig.getNotification().add(notification);
		AlarmNotificationConfigDao dao = new AlarmNotificationConfigDao();
		dao.deleteNotification(alarmNorthbounderConfig,
				"/home/tchandra123/chitra/Others/alarmNotificationConf.xml");
	}

}
