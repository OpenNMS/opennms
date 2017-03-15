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

package org.opennms.netmgt.config.notifications;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class NotificationsTest extends XmlTestNoCastor<Notifications> {

    public NotificationsTest(Notifications sampleObject, Object sampleXml) {
        super(sampleObject, sampleXml, "src/main/resources/xsds/notifications.xsd");
    }

    @Parameters
    public static Collection<Object[]> data() throws ParseException {
        return Arrays.asList(new Object[][] {
            {
                getNotifications(),
                "<notifications xmlns=\"http://xmlns.opennms.org/xsd/notifications\">\n" + 
                "    <header>\n" + 
                "        <rev>1.2</rev>\n" + 
                "        <created>Wednesday, February 6, 2002 10:10:00 AM EST</created>\n" + 
                "        <mstation>localhost</mstation>\n" + 
                "    </header>\n" + 
                "    <notification name=\"interfaceDown\" status=\"on\">\n" + 
                "        <uei>uei.opennms.org/nodes/interfaceDown</uei>\n" + 
                "        <rule>IPADDR != '0.0.0.0'</rule>\n" + 
                "        <destinationPath>Email-Admin</destinationPath>\n" + 
                "        <text-message>All services are down</text-message>\n" + 
                "        <subject>Notice #%noticeid%: %interfaceresolve% (%interface%) on node %nodelabel% down.</subject>\n" + 
                "        <numeric-message>111-%noticeid%</numeric-message>\n" + 
                "    </notification>\n" + 
                "</notifications>"
            }
        });
    }

    private static Notifications getNotifications() {
        Notifications notifications = new Notifications();

        Header header = new Header();
        header.setRev("1.2");
        header.setCreated("Wednesday, February 6, 2002 10:10:00 AM EST");
        header.setMstation("localhost");
        notifications.setHeader(header);

        Notification notification = new Notification();
        notification.setName("interfaceDown");
        notification.setStatus("on");
        notification.setUei("uei.opennms.org/nodes/interfaceDown");
        notification.setRule("IPADDR != '0.0.0.0'");
        notification.setDestinationPath("Email-Admin");
        notification.setTextMessage("All services are down");
        notification.setSubject("Notice #%noticeid%: %interfaceresolve% (%interface%) on node %nodelabel% down.");
        notification.setNumericMessage("111-%noticeid%");
        notifications.addNotification(notification);

        return notifications;
    }
}
