/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

import java.util.Date;

import org.junit.ClassRule;
import org.junit.Test;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.hibernate.EventDaoHibernate;
import org.opennms.netmgt.dao.hibernate.NodeDaoHibernate;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.PrimaryType;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.opennms.netmgt.provision.persist.requisition.RequisitionInterface;
import org.opennms.netmgt.provision.persist.requisition.RequisitionNode;
import org.opennms.smoketest.containers.MinionContainer;
import org.opennms.smoketest.stacks.OpenNMSStack;
import org.opennms.smoketest.stacks.MinionProfile;
import org.opennms.smoketest.stacks.StackModel;
import org.opennms.smoketest.utils.DaoUtils;
import org.opennms.smoketest.utils.RestClient;
import org.opennms.smoketest.utils.SyslogUtils;

import com.google.common.collect.ImmutableList;

/**
 * Verifies that syslog messages sent to the Minion from nodes with
 * identical IP addresses are associated with the correct node.
 *
 * See https://issues.opennms.org/browse/NMS-8798
 *
 * @author fooker
 */
public class SyslogOverlappingIpAddressIT {

    @ClassRule
    public static final OpenNMSStack stack = OpenNMSStack.withModel(StackModel.newBuilder()
            // Add 2 Minions at different locations
            .withMinions(MinionProfile.DEFAULT, MinionProfile.newBuilder()
                    .withLocation("BANANA")
                    .build())
            .build());

    @Test
    public void testAssociateSyslogsWithNodesWithOverlappingIpAddresses() {
        final MinionContainer minion1 = stack.minions(0);
        final MinionContainer minion2 = stack.minions(1);

        // We expect the minions to be at different locations
        assertThat(minion1.getLocation(), not(equalTo(minion2.getLocation())));

        final Date startOfTest = new Date();
        final String hostIpAddress = "1.2.3.4";

        // Create requisition with two node in different locations but same IP
        final RestClient client = stack.opennms().getRestClient();
        final Requisition requisition = new Requisition("overlapping");
        final RequisitionNode node1 = new RequisitionNode();
        node1.setNodeLabel("node_1");
        node1.setLocation(minion1.getLocation());
        final RequisitionInterface interface1 = new RequisitionInterface();
        interface1.setIpAddr(hostIpAddress);
        interface1.setManaged(true);
        interface1.setSnmpPrimary(PrimaryType.PRIMARY);
        node1.setInterfaces(ImmutableList.of(interface1));
        node1.setForeignId("node_1");
        requisition.insertNode(node1);
        final RequisitionNode node2 = new RequisitionNode();
        node2.setNodeLabel("node_2");
        node2.setLocation(minion2.getLocation());
        final RequisitionInterface interface2 = new RequisitionInterface();
        interface2.setIpAddr(hostIpAddress);
        interface2.setManaged(true);
        interface2.setSnmpPrimary(PrimaryType.PRIMARY);
        node2.setInterfaces(ImmutableList.of(interface2));
        node2.setForeignId("node_2");
        requisition.insertNode(node2);
        client.addOrReplaceRequisition(requisition);
        client.importRequisition("overlapping");

        // Wait for the nodes to be provisioned
        final NodeDaoHibernate nodeDao = stack.postgres().dao(NodeDaoHibernate.class);
        final EventDaoHibernate eventDao = stack.postgres().dao(EventDaoHibernate.class);
        final OnmsNode onmsNode1 = await()
                .atMost(2, MINUTES).pollInterval(5, SECONDS)
                .until(DaoUtils.findMatchingCallable(nodeDao,
                        new CriteriaBuilder(OnmsNode.class)
                                .eq("label", "node_1")
                                .toCriteria()),
                        notNullValue());
        final OnmsNode onmsNode2 = await()
                .atMost(1, MINUTES).pollInterval(5, SECONDS)
                .until(DaoUtils.findMatchingCallable(nodeDao,
                        new CriteriaBuilder(OnmsNode.class)
                                .eq("label", "node_2")
                                .toCriteria()),
                        notNullValue());

        // Sending syslog messages to each node and expect it to appear on the node
        SyslogUtils.sendMessage(minion1.getSyslogAddress(), hostIpAddress, 1);
        await().atMost(1, MINUTES).pollInterval(5, SECONDS)
                .until(DaoUtils.countMatchingCallable(eventDao,
                        new CriteriaBuilder(OnmsEvent.class)
                                .eq("eventUei", "uei.opennms.org/vendor/cisco/syslog/SEC-6-IPACCESSLOGP/aclDeniedIPTraffic")
                                .ge("eventCreateTime", startOfTest)
                                .eq("node", onmsNode1)
                                .toCriteria()),
                        is(1));

        SyslogUtils.sendMessage(minion2.getSyslogAddress(), hostIpAddress, 1);
        await().atMost(1, MINUTES).pollInterval(5, SECONDS)
                .until(DaoUtils.countMatchingCallable(eventDao,
                        new CriteriaBuilder(OnmsEvent.class)
                                .eq("eventUei", "uei.opennms.org/vendor/cisco/syslog/SEC-6-IPACCESSLOGP/aclDeniedIPTraffic")
                                .ge("eventCreateTime", startOfTest)
                                .eq("node", onmsNode2)
                                .toCriteria()),
                        is(1));
    }


}
