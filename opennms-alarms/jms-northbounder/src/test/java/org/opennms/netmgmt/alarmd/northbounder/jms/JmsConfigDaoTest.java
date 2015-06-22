package org.opennms.netmgmt.alarmd.northbounder.jms;
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opennms.netmgt.alarmd.northbounder.jms.JmsDestination;
import org.opennms.netmgt.alarmd.northbounder.jms.JmsNorthbounderConfig;
import org.opennms.netmgt.alarmd.northbounder.jms.JmsNorthbounderConfigDao;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

public class JmsConfigDaoTest {
	
	String xmlAsXmlFirstOnlySomeUeis = "" +
			"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
			"<jms-northbounder-config>" +
			"  <enabled>true</enabled>" +
			"  <message-format>ALARM ID:${alarmId} NODE:${nodeLabel}</message-format>" +
			">\n" +
			"  <destination>" +
			"    <jms-destination>OpenNMSAlarmQueue</jms-destination>" +
			"    <send-as-object-message>false</send-as-object-message>" +
			"    <first-occurence-only>true</first-occurence-only>" +
			">\n" +
			"   </destination>" +
			"	<uei>uei.opennms.org/nodes/nodeDown</uei>\n" +
			"	<uei>uei.opennms.org/nodes/nodeUp</uei>\n" +
			"</jms-northbounder-config>\n" +
			"";
	
	@Test
        public void testAsXmlQueueFirstAlarmSomeUeis() throws InterruptedException {
                
                Resource resource = new ByteArrayResource(xmlAsXmlFirstOnlySomeUeis.getBytes());
                                
                JmsNorthbounderConfigDao dao = new JmsNorthbounderConfigDao();
                dao.setConfigResource(resource);
                dao.afterPropertiesSet();
                
                JmsNorthbounderConfig config = dao.getConfig();
                
                assertNotNull(config);
                
                assertEquals(true, config.isEnabled());
                assertEquals(new Integer("1000"), config.getNaglesDelay());
                assertEquals(new Integer(100), config.getBatchSize());
                assertEquals(new Integer(300000), config.getQueueSize());
                assertEquals("ALARM ID:${alarmId} NODE:${nodeLabel}", config.getMessageFormat());
                
                JmsDestination jmsDestination = config.getDestinations().get(0);
                assertNotNull(jmsDestination);
                assertEquals("OpenNMSAlarmQueue", jmsDestination.getJmsDestination());
                assertEquals("QUEUE", jmsDestination.getDestinationType().toString());
                assertEquals(true, jmsDestination.isFirstOccurrenceOnly());
                assertEquals(false, jmsDestination.isSendAsObjectMessageEnabled());
                assertEquals(null,  jmsDestination.getMessageFormat());
                
                assertEquals("uei.opennms.org/nodes/nodeDown", config.getUeis().get(0));
                assertEquals("uei.opennms.org/nodes/nodeUp", config.getUeis().get(1));
                
        }

	String xmlAsObjectMessageTopicAllAlarmsNoUeis = "" +
			"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
			"<jms-northbounder-config>" +
			"  <enabled>false</enabled>" +
			"  <nagles-delay>10000</nagles-delay>" +
			"  <batch-size>10</batch-size>" +
			"  <queue-size>100</queue-size>" +
			">\n" +
			"  <destination>" +
			"    <jms-destination>OpenNMSAlarmTopic</jms-destination>" +
			"    <destination-type>TOPIC</destination-type>" +
                        "    <send-as-object-message>true</send-as-object-message>" +
                        "    <first-occurence-only>false</first-occurence-only>" +
                        ">\n" +
			"   </destination>" +
			"</jms-northbounder-config>\n" +
			"";

	
	@Test
	public void testAsObjectMessageTopicAllAlarmsNoUeis() {
		Resource resource = new ByteArrayResource(xmlAsObjectMessageTopicAllAlarmsNoUeis.getBytes());
		
		JmsNorthbounderConfigDao dao = new JmsNorthbounderConfigDao();
		dao.setConfigResource(resource);
		
		dao.afterPropertiesSet();
		
		JmsNorthbounderConfig config = dao.getConfig();
		
		assertNotNull(config);
		assertEquals(null, config.getUeis());
		assertTrue(!config.getDestinations().get(0).isFirstOccurrenceOnly());
		assertEquals(true, config.getDestinations().get(0).isSendAsObjectMessageEnabled());
		assertEquals("TOPIC", config.getDestinations().get(0).getDestinationType().toString());
		assertEquals(false, config.isEnabled());
                assertEquals(new Integer("10000"), config.getNaglesDelay());
                assertEquals(new Integer(10), config.getBatchSize());
                assertEquals(new Integer(100), config.getQueueSize());
	}
	
}
