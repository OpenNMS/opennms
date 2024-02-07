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
package org.opennms.smoketest;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertEquals;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.Response;

import org.junit.ClassRule;
import org.junit.Test;
import org.opennms.smoketest.stacks.OpenNMSStack;
import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.api.NotificationDao;
import org.opennms.netmgt.dao.hibernate.NotificationDaoHibernate;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsNotification;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.smoketest.utils.DaoUtils;
import org.opennms.smoketest.utils.RestClient;

public class NotifdIT {

    private static final String NODE_LABEL = "node1";
    private static final String IP_ADDRESS = "192.168.1.1";

    @ClassRule
    public static OpenNMSStack stack = OpenNMSStack.MINIMAL;

    @Test
    public void testNotifdAutoAcknowledgeAlarm() {
        RestClient restClient = stack.opennms().getRestClient();

        //Add a node with interface
        OnmsNode node = new OnmsNode();
        node.setLabel(NODE_LABEL);
        node.setType(OnmsNode.NodeType.ACTIVE);
        node.setForeignSource("test");
        node.setForeignId(NODE_LABEL);
        Response response = restClient.addNode(node);
        assertEquals(201, response.getStatus());
        node = restClient.getNode("test:" + NODE_LABEL);
        OnmsIpInterface ipInterface = new OnmsIpInterface();
        ipInterface.setNode(node);
        ipInterface.setIpAddress(InetAddressUtils.getInetAddress(IP_ADDRESS));
        ipInterface.setIpHostName(IP_ADDRESS);
        response = restClient.addInterface("test:" + NODE_LABEL, ipInterface);
        assertEquals(201, response.getStatus());

        // Send a node down event
        EventBuilder builder = new EventBuilder(EventConstants.NODE_DOWN_EVENT_UEI, "test", new Date());
        builder.setNode(node);
        restClient.sendEvent(builder.getEvent());

        // Check that a notification is generated for node down
        NotificationDao notificationDao = stack.postgres().dao(NotificationDaoHibernate.class);
        Criteria criteria = new CriteriaBuilder(OnmsNotification.class).createAlias("node", "node")
                .eq("node.id", node.getId()).toCriteria();
        await().atMost(30, TimeUnit.SECONDS).pollInterval(10, TimeUnit.SECONDS)
                .until(DaoUtils.countMatchingCallable(notificationDao, criteria), greaterThan(0));

        // Send a node Up event
        EventBuilder builder1 = new EventBuilder(EventConstants.NODE_UP_EVENT_UEI, "test", new Date());
        builder1.setNode(node);
        restClient.sendEvent(builder1.getEvent());

        // Check that notification is auto-acknowledged
        criteria = new CriteriaBuilder(OnmsNotification.class).eq("answeredBy", "auto-acknowledged")
                .createAlias("node", "node").eq("node.id", node.getId()).toCriteria();

        await().atMost(1, TimeUnit.MINUTES).pollInterval(5, TimeUnit.SECONDS)
                .until(DaoUtils.countMatchingCallable(notificationDao, criteria), greaterThan(0));
    }

}
