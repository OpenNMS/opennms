/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016-2017 The OpenNMS Group, Inc.
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

package org.opennms.smoketest.minion;

import static com.jayway.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.ClassRule;
import org.junit.Test;
import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.api.EventDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.hibernate.EventDaoHibernate;
import org.opennms.netmgt.dao.hibernate.NodeDaoHibernate;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.resource.ResourceDTO;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Value;
import org.opennms.smoketest.stacks.NetworkProtocol;
import org.opennms.smoketest.stacks.OpenNMSStack;
import org.opennms.smoketest.telemetry.Packet;
import org.opennms.smoketest.telemetry.Packets;
import org.opennms.smoketest.telemetry.Sender;
import org.opennms.smoketest.utils.DaoUtils;
import org.opennms.smoketest.utils.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Verifies that Telemetry listeners can receive proto buffers and generate rrd
 * files
 *
 * @author cgorantla
 */

public class JtiTelemetryIT {

    private static final Logger LOG = LoggerFactory.getLogger(JtiTelemetryIT.class);
    public static final String SENDER_IP = "192.168.1.1";

    @ClassRule
    public static final OpenNMSStack stack = OpenNMSStack.MINION;

    @Test
    public void verifyJtiTelemetryOnOpenNMS() throws Exception {
        final Date startOfTest = new Date();
        final OnmsNode onmsNode = sendNewSuspectEvent(stack, false, startOfTest);
        final InetSocketAddress opennmsJtiPort = stack.opennms().getNetworkProtocolAddress(NetworkProtocol.JTI);
        await().atMost(1, MINUTES).pollDelay(0, SECONDS).pollInterval(5, SECONDS)
                .until(() -> {
                    sendJtiTelemetryMessage(opennmsJtiPort);
                    return matchRrdFileFromNodeResource(onmsNode.getId());
                });
    }

    @Test
    public void verifyJtiTelemetryOnMinion() throws Exception {
        final Date startOfTest = new Date();
        final OnmsNode onmsNode = sendNewSuspectEvent(stack, true, startOfTest);
        final InetSocketAddress minionJtiPort = stack.minion().getNetworkProtocolAddress(NetworkProtocol.JTI);
        await().atMost(2, MINUTES).pollDelay(0, SECONDS).pollInterval(5, SECONDS)
                .until(() -> {
                    sendJtiTelemetryMessage(minionJtiPort);
                    return matchRrdFileFromNodeResource(onmsNode.getId());
                });
    }

    public static void sendJtiTelemetryMessage(InetSocketAddress udpAddress) {
        try {
            new Packet(Packets.JTI.getPayload()).send(Sender.udp(udpAddress));
        } catch (IOException e) {
            LOG.error("Exception while sending jti packets", e);
        }
    }

    public static boolean matchRrdFileFromNodeResource(Integer id)  {
        final RestClient client = stack.opennms().getRestClient();
        final ResourceDTO resources = client.getResourcesForNode(Integer.toString(id));
        return resources.getChildren().getObjects().stream()
                .flatMap(r -> r.getRrdGraphAttributes().values().stream())
                .anyMatch(a -> a.getRrdFile().startsWith("ifOutOctets"));
    }

    public static OnmsNode sendNewSuspectEvent(OpenNMSStack stack, boolean isMinion, Date startOfTest)
            throws IOException {

        Event minionEvent = new Event();
        minionEvent.setUei("uei.opennms.org/internal/discovery/newSuspect");
        minionEvent.setHost(SENDER_IP);
        minionEvent.setInterface(SENDER_IP);
        minionEvent.setInterfaceAddress(Inet4Address.getByName(SENDER_IP));
        minionEvent.setSource("system-test");
        minionEvent.setSeverity("4");
        if (isMinion) {
            Parm parm = new Parm();
            parm.setParmName("location");
            Value minion = new Value(stack.minion().getLocation());
            parm.setValue(minion);
            List<Parm> parms = new ArrayList<>();
            parms.add(parm);
            minionEvent.setParmCollection(parms);
        }
        stack.opennms().getRestClient().sendEvent(minionEvent);

        EventDao eventDao = stack.postgres().dao(EventDaoHibernate.class);
        NodeDao nodeDao = stack.postgres().dao(NodeDaoHibernate.class);

        Criteria criteria = new CriteriaBuilder(OnmsEvent.class)
                .eq("eventUei", EventConstants.NEW_SUSPECT_INTERFACE_EVENT_UEI).ge("eventTime", startOfTest)
                .eq("ipAddr", Inet4Address.getByName(SENDER_IP)).toCriteria();

        await().atMost(1, MINUTES).pollInterval(10, SECONDS).until(DaoUtils.countMatchingCallable(eventDao, criteria),
                greaterThan(0));

        final OnmsNode onmsNode = await().atMost(1, MINUTES).pollInterval(5, SECONDS)
                .until(DaoUtils.findMatchingCallable(nodeDao, new CriteriaBuilder(OnmsNode.class).eq("label", SENDER_IP)
                        .ge("createTime", startOfTest).toCriteria()), notNullValue());

        assertNotNull(onmsNode);

        if (isMinion) {
            assertThat(onmsNode.getLocation().getLocationName(), is(stack.minion().getLocation()));
        }

        LOG.info("New suspect event has been sent and node has been created for IP : {}", SENDER_IP);
        return onmsNode;
    }
}
