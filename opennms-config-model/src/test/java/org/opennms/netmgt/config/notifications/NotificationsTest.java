/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
        Rule filterRule = new Rule();
        filterRule.setContent("IPADDR != '0.0.0.0'");
        notification.setRule(filterRule);
        notification.setDestinationPath("Email-Admin");
        notification.setTextMessage("All services are down");
        notification.setSubject("Notice #%noticeid%: %interfaceresolve% (%interface%) on node %nodelabel% down.");
        notification.setNumericMessage("111-%noticeid%");
        notifications.addNotification(notification);

        return notifications;
    }
}
