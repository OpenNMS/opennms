/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.notifd;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class NotifdConfigurationTest extends XmlTestNoCastor<NotifdConfiguration> {

    public NotifdConfigurationTest(NotifdConfiguration sampleObject, Object sampleXml) {
        super(sampleObject, sampleXml, "src/main/resources/xsds/notifd-configuration.xsd");
    }

    @Parameters
    public static Collection<Object[]> data() throws ParseException {
        return Arrays.asList(new Object[][] {
            {
                getNotifdConfiguration(),
                "<notifd-configuration \n" + 
                "        status=\"off\"\n" + 
                "        match-all=\"true\">\n" +
                "        <auto-acknowledge-alarm resolution-prefix=\"BLAH!\" notify=\"false\">\n" +
                "                <uei>yo</uei>\n" +
                "        </auto-acknowledge-alarm>\n" +
                "        <auto-acknowledge resolution-prefix=\"RESOLVED: \" \n" +
                "                          uei=\"uei.opennms.org/nodes/nodeRegainedService\" \n" +
                "                          acknowledge=\"uei.opennms.org/nodes/nodeLostService\">\n" +
                "                          <match>nodeid</match>\n" +
                "                          <match>interfaceid</match>\n" +
                "                          <match>serviceid</match>\n" +
                "        </auto-acknowledge>\n" +
                "        <queue>\n" + 
                "                <queue-id>default</queue-id>\n" + 
                "                <interval>20s</interval>\n" + 
                "                <handler-class>\n" + 
                "                        <name>org.opennms.netmgt.notifd.DefaultQueueHandler</name>\n" + 
                "                </handler-class>\n" + 
                "        </queue>\n" +
                "</notifd-configuration>"
            }
        });
    }

    private static NotifdConfiguration getNotifdConfiguration() {
        NotifdConfiguration config = new NotifdConfiguration();
        config.setStatus("off");
        config.setMatchAll(true);

        final AutoAcknowledgeAlarm aaa = new AutoAcknowledgeAlarm();
        aaa.setNotify(false);
        aaa.setResolutionPrefix("BLAH!");
        aaa.addUei("yo");
        config.setAutoAcknowledgeAlarm(aaa);

        AutoAcknowledge autoAck = new AutoAcknowledge();
        autoAck.setResolutionPrefix("RESOLVED: ");
        autoAck.setUei("uei.opennms.org/nodes/nodeRegainedService");
        autoAck.setAcknowledge("uei.opennms.org/nodes/nodeLostService");
        autoAck.addMatch("nodeid");
        autoAck.addMatch("interfaceid");
        autoAck.addMatch("serviceid");
        config.addAutoAcknowledge(autoAck);

        Queue queue = new Queue();
        queue.setQueueId("default");
        queue.setInterval("20s");
        HandlerClass handlerClass = new HandlerClass();
        handlerClass.setName("org.opennms.netmgt.notifd.DefaultQueueHandler");
        queue.setHandlerClass(handlerClass);
        config.addQueue(queue);

        return config;
    }
}
