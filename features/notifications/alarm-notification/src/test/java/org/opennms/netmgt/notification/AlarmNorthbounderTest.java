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
package org.opennms.netmgt.notification;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.opennms.netmgt.alarmd.api.NorthboundAlarm;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.model.TroubleTicketState;
import org.opennms.netmgt.notification.filter.DroolsFileLoader;
import org.opennms.netmgt.notification.parser.AlarmNorthbounderConfig;
import org.opennms.netmgt.notification.parser.AlarmNotificationConfigDao;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

@Ignore
public class AlarmNorthbounderTest {

	@Test
	@Ignore
	public void testAlarmNorthbounder() {
		AlarmNorthbounderConfig config = createConfig();
		try {
			assertNotNull(new AlarmNorthbounder(config));
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private AlarmNorthbounderConfig createConfig() {
		try {
			FileInputStream fstream = new FileInputStream(
					"/home/tchandra123/opennms/src/opennms/"
							+ "features/notifications/alarm-notification/src/main/etc//alarm-notification/alarmNotificationConf.xml");
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
			return config;
		} catch (Exception e) {
			System.err.println("Error in AlarmNorthBounderTest createConfig");
		}
		return null;
	}

	private NorthboundAlarm createNorthboundAlarm() {
		OnmsAlarm onmsAlarm = new OnmsAlarm();
		onmsAlarm.setAlarmAckTime(new Date(11111111));
		onmsAlarm.setAlarmAckUser("TestUser");
		onmsAlarm.setAlarmType(1);
		onmsAlarm.setApplicationDN("applicationDN");
		onmsAlarm.setReductionKey("reductionkey");
		onmsAlarm.setClearKey("clearKey");
		onmsAlarm.setCounter(2);
		onmsAlarm.setDescription("description");
		onmsAlarm.setDistPoller(new OnmsDistPoller());
		onmsAlarm.setEventParms("eventparms");
		onmsAlarm.setFirstAutomationTime(new Date(111111111));
		onmsAlarm.setFirstEventTime(new Date(1111111111));
		onmsAlarm.setId(56);
		try {
			onmsAlarm.setIpAddr(InetAddress.getByName("127.0.0.1"));
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		onmsAlarm.setLastAutomationTime(new Date(23333333));
		onmsAlarm.setLastEventTime(new Date(475958459));
		onmsAlarm.setLogMsg("logmsg");
		onmsAlarm.setOperInstruct("operinstruct");
		onmsAlarm.setOssPrimaryKey("key");
		OnmsServiceType serviceType = new OnmsServiceType("servicename");
		serviceType.setId(10);
		onmsAlarm.setServiceType(serviceType);
		onmsAlarm.setSeverityId(5);
		onmsAlarm.setSuppressedTime(new Date(23333333));
		onmsAlarm.setSuppressedUntil(new Date(657843));
		onmsAlarm.setSuppressedUser("suppresseduser");
		onmsAlarm.setTTicketId("tticketid");
		onmsAlarm.setTTicketState(TroubleTicketState.valueOf("RESOLVED"));

		onmsAlarm.setUei("uei.opennms.org/generic/traps/SNMP_Link_Down");
		onmsAlarm.setX733AlarmType("alarmType");
		onmsAlarm.setX733ProbableCause(0);

		NorthboundAlarm northBoundAlarm = new NorthboundAlarm(onmsAlarm);
		return northBoundAlarm;
	}

	private NorthboundAlarm createClearlarm() {
		OnmsAlarm onmsAlarm = new OnmsAlarm();
		onmsAlarm.setAlarmAckTime(new Date(11111111));
		onmsAlarm.setAlarmAckUser("TestUser");
		onmsAlarm.setAlarmType(1);
		onmsAlarm.setReductionKey("fdsfdsfds");
		onmsAlarm.setApplicationDN("applicationDN");
		onmsAlarm.setCounter(2);
		onmsAlarm.setDescription("description");
		onmsAlarm.setDistPoller(new OnmsDistPoller());
		onmsAlarm.setEventParms("eventparms");
		onmsAlarm.setFirstAutomationTime(new Date(111111111));
		onmsAlarm.setFirstEventTime(new Date(1111111111));
		onmsAlarm.setId(56);
		try {
			onmsAlarm.setIpAddr(InetAddress.getByName("127.0.0.1"));
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		onmsAlarm.setLastAutomationTime(new Date(23333333));
		onmsAlarm.setLastEventTime(new Date(475958459));
		onmsAlarm.setLogMsg("logmsg");
		onmsAlarm.setOperInstruct("operinstruct");
		onmsAlarm.setOssPrimaryKey("key");
		OnmsServiceType serviceType = new OnmsServiceType("servicename");
		serviceType.setId(10);
		onmsAlarm.setServiceType(serviceType);
		onmsAlarm.setSeverityId(5);
		onmsAlarm.setSuppressedTime(new Date(23333333));
		onmsAlarm.setSuppressedUntil(new Date(657843));
		onmsAlarm.setSuppressedUser("suppresseduser");
		onmsAlarm.setTTicketId("tticketid");
		onmsAlarm.setTTicketState(TroubleTicketState.valueOf("RESOLVED"));

		onmsAlarm.setUei("uei.opennms.org/vendor/Juniper/traps/jnxVpnIfUp");
		onmsAlarm.setX733AlarmType("alarmType");
		onmsAlarm.setX733ProbableCause(0);

		NorthboundAlarm northBoundAlarm = new NorthboundAlarm(onmsAlarm);
		return northBoundAlarm;
	}

	@Test
	@Ignore
	public void testAcceptsNorthboundAlarm() {

		AlarmNorthbounderConfig config = createConfig();
		try {
			AlarmNorthbounder alarmNorthBounder = new AlarmNorthbounder(config);
			NorthboundAlarm northboundAlarm = createNorthboundAlarm();
			assertFalse(alarmNorthBounder.accepts(northboundAlarm));
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testForwardAlarms() {
		AlarmNorthbounderConfig config = createConfig();
		try {
			String drlName = "Ip_10.212.96.214.drl";
			new DroolsFileLoader();
			AlarmNorthbounder alarmNorthBounder = new AlarmNorthbounder(config);
			NorthboundAlarm northboundAlarm = createNorthboundAlarm();
			alarmNorthBounder.accepts(northboundAlarm);
			List<NorthboundAlarm> northboundAlarmList = new ArrayList<NorthboundAlarm>();
			northboundAlarmList.add(northboundAlarm);
			alarmNorthBounder.forwardAlarms(northboundAlarmList);

			// northboundAlarm = createClearlarm();
			// alarmNorthBounder.accepts(northboundAlarm);
			// northboundAlarmList = new ArrayList<NorthboundAlarm>();
			// northboundAlarmList.add(northboundAlarm);
			//
			// //assertNotNull(DroolsFileLoader.getKnowledgeBaseForDrl(drlName));
			// alarmNorthBounder.forwardAlarms(northboundAlarmList);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
