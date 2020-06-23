/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.smoketest;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.opennms.netmgt.events.api.EventConstants.ABSOLUTE_CHANGE_THRESHOLD_EVENT_UEI;
import static org.opennms.netmgt.events.api.EventConstants.HIGH_THRESHOLD_EVENT_UEI;

import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsAlarmCollection;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsEventCollection;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMetaData;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.smoketest.stacks.OpenNMSProfile;
import org.opennms.smoketest.stacks.OpenNMSStack;
import org.opennms.smoketest.stacks.StackModel;
import org.opennms.smoketest.utils.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests to validate that thresholding works.
 *
 * @author jwhite
 */
public class ThresholdingIT {
    private static final Logger LOG = LoggerFactory.getLogger(ThresholdingIT.class);

    private static final String TEST_NODE_FS = "test";
    private static final String TEST_NODE_FID = "thresholding-test-node";
    private static final String TEST_NODE_CRITERIA = TEST_NODE_FS + ":" + TEST_NODE_FID;
    private static final String TEST_NODE_CATEGORY = "ThresholdingTest";
    private static final String TEST_SVC_NAME = "SvcToThreshold";

    @ClassRule
    public static final OpenNMSStack stack = OpenNMSStack.withModel(StackModel.newBuilder()
            .withOpenNMS(OpenNMSProfile.newBuilder()
                    .withFile("thresholding/poller-configuration.xml", "etc/poller-configuration.xml")
                    .withFile("thresholding/collectd-configuration.xml", "etc/collectd-configuration.xml")
                    .withFile("thresholding/jdbc-datacollection-config.xml", "etc/jdbc-datacollection-config.xml")
                    .withFile("thresholding/resource-types.d/metadata.xml", "etc/resource-types.d/metadata.xml")
                    .withFile("thresholding/threshd-configuration.xml", "etc/threshd-configuration.xml")
                    .withFile("thresholding/thresholds.xml", "etc/thresholds.xml")
                    .withFile("thresholding/thresholding-test-monitor.sh", "bin/thresholding-test-monitor.sh",
                            // Make the script executable
                            PosixFilePermissions.fromString("rwxrwxr-x"))
                    .build())
            .build());

    private RestClient restClient;

    @Before
    public void setUp() {
        restClient = stack.opennms().getRestClient();
        // Delete the test node from a possible previous run
        restClient.deleteNode(TEST_NODE_CRITERIA);
    }

    /**
     * Goal: We want to trigger a threshold based on response time data from a service
     * monitored by the poller.
     *
     * We achieve this by configuring a SystemExecuteMonitor for a node where the amount
     * of time to sleep is passed in as a command line argument and is populated by the node
     * meta-data. Controlling the delay then becomes as simple as changing the meta-data value.
     *
     */
    @Test
    public void canTriggerHighResponseTimeThreshold() {
        // Create our test node
        LOG.info("Setting up test node...");
        final OnmsNode testNode = addNode();
        // Validate that the was associated with the expected category
        final Set<String> categoriesOnNode = testNode.getCategories().stream()
                .map(OnmsCategory::getName)
                .collect(Collectors.toSet());
        assertThat(categoriesOnNode, hasItem(TEST_NODE_CATEGORY));

        // Verify that no existing highThresholdExceeded alarm exists for the node
        assertThat(getAlarmsUeisForNode(testNode.getId()), not(hasItem(HIGH_THRESHOLD_EVENT_UEI)));

        // Increase the service delay above the threshold limit
        setServiceDelay(6, TimeUnit.SECONDS);

        // Wait for the high threshold to appear
        LOG.info("Waiting for high threshold alarm...");
        await().atMost(1, TimeUnit.MINUTES).pollInterval(5, TimeUnit.SECONDS)
                .until(() -> getAlarmsUeisForNode(testNode.getId()), hasItem(HIGH_THRESHOLD_EVENT_UEI));

        // Now remove the delay
        setServiceDelay(0, TimeUnit.SECONDS);

        LOG.info("Waiting for high threshold alarm to clear...");
        await().atMost(1, TimeUnit.MINUTES).pollInterval(5, TimeUnit.SECONDS)
                .until(() -> restClient.getAlarmsByEventUei(HIGH_THRESHOLD_EVENT_UEI)
                        .getObjects().get(0).getSeverity(), equalTo(OnmsSeverity.CLEARED));
    }

    /**
     * Goal: We want to trigger a threshold based on metrics collected by collectd.
     *
     * We achieve this by configuring the JDBC collector to collect values from the node_metadata table,
     * storing these in generic type resources, and thresholding on these values.
     *
     * Controlling the thresholds becomes as simple as changing the meta-data values.
     */
    @Test
    public void canTriggerAbsoluteChangeThreshold() {
        // Create our test node
        LOG.info("Setting up test node...");
        final OnmsNode testNode = addNode();
        // Validate that the was associated with the expected category
        final Set<String> categoriesOnNode = testNode.getCategories().stream()
                .map(OnmsCategory::getName)
                .collect(Collectors.toSet());
        assertThat(categoriesOnNode, hasItem(TEST_NODE_CATEGORY));

        // Verify that no existing absoluteChangeExceeded event exists for the node
        assertThat(getEventsUeisForNode(testNode.getId()), not(hasItem(ABSOLUTE_CHANGE_THRESHOLD_EVENT_UEI)));

        // We use a another distinct meta-data attribute since there's a bug with JDBC collector whereby it doesn't
        // build generic type resources if there is only one result in the result-set
        final AtomicLong currentValue = new AtomicLong(0);
        final int absoluteChangeThreshold = 10;

        // Wait for the high threshold to appear
        LOG.info("Waiting for absolute change threshold event...");
        await().atMost(2, TimeUnit.MINUTES).pollInterval(10, TimeUnit.SECONDS).pollDelay(0, TimeUnit.SECONDS)
                .until(() -> {
                    // Keep increasing the value until the threshold is hit
                    setServiceJitter(currentValue.getAndAdd(absoluteChangeThreshold), TimeUnit.SECONDS);
                    return getEventsUeisForNode(testNode.getId());
                }, hasItem(ABSOLUTE_CHANGE_THRESHOLD_EVENT_UEI));
    }

    private Set<String> getAlarmsUeisForNode(int nodeId) {
        final OnmsAlarmCollection alarms = restClient.getAlarmsForNode(nodeId);
        return alarms.getObjects().stream().map(OnmsAlarm::getUei).collect(Collectors.toSet());
    }

    private Set<String> getEventsUeisForNode(int nodeId) {
        final OnmsEventCollection events = restClient.getEventsForNode(nodeId);
        return events.getObjects().stream().map(OnmsEvent::getEventUei).collect(Collectors.toSet());
    }

    private void setServiceDelay(long duration, TimeUnit unit) {
        LOG.info("Updating service delay to {} {}.", duration, unit);
        OnmsMetaData metaData = new OnmsMetaData("test", "svc-delay", Long.toString(unit.toMillis(duration)));
        Response response = restClient.setNodeLevelMetadata(TEST_NODE_CRITERIA, metaData);
        assertThat(response.getStatus(), equalTo(HttpServletResponse.SC_NO_CONTENT));
    }

    private void setServiceJitter(long duration, TimeUnit unit) {
        LOG.info("Updating service jitter to {} {}.", duration, unit);
        OnmsMetaData metaData = new OnmsMetaData("test", "svc-jitter", Long.toString(unit.toMillis(duration)));
        Response response = restClient.setNodeLevelMetadata(TEST_NODE_CRITERIA, metaData);
        assertThat(response.getStatus(), equalTo(HttpServletResponse.SC_NO_CONTENT));
    }

    private OnmsNode addNode() {
        // Create a node
        OnmsNode node = new OnmsNode();
        // Set foreignSource and foreignId to use it as nodeCriteria
        node.setForeignSource(TEST_NODE_FS);
        node.setForeignId(TEST_NODE_FID);
        node.setLabel(TEST_NODE_FID);
        node.setType(OnmsNode.NodeType.ACTIVE);

        // Create the node
        Response response = restClient.addNode(node);
        assertThat(response.getStatus(), equalTo(HttpServletResponse.SC_CREATED));

        // Create the category
        try {
            restClient.addCategory(TEST_NODE_CATEGORY);
        } catch (Exception e) {
            // Ignore exceptions if the category already exists
        }

        // Now add a category to the node
        restClient.addCategoryToNode(TEST_NODE_CRITERIA, TEST_NODE_CATEGORY);

        // Add meta-data to the node
        OnmsMetaData metaData = new OnmsMetaData();
        metaData.setContext("test");
        metaData.setKey("svc-delay");
        metaData.setValue(Integer.toString(0));
        response = restClient.setNodeLevelMetadata(TEST_NODE_CRITERIA, metaData);
        assertThat(response.getStatus(), equalTo(HttpServletResponse.SC_NO_CONTENT));

        // Add Interface to node
        OnmsIpInterface iface = new OnmsIpInterface();
        iface.setNode(node);
        iface.setIsManaged("M");
        iface.setIpAddress(InetAddressUtils.getInetAddress("192.168.1.1"));
        iface.setIpHostName("192.168.1.1");
        response = restClient.addInterface(TEST_NODE_CRITERIA, iface);
        assertThat(response.getStatus(), equalTo(HttpServletResponse.SC_CREATED));

        // Add a service on interface (192.168.1.1)
        OnmsMonitoredService svc = new OnmsMonitoredService();
        OnmsServiceType serviceType = new OnmsServiceType();
        serviceType.setName(TEST_SVC_NAME);
        svc.setServiceType(serviceType);
        svc.setStatus("A");
        svc.setIpInterface(iface);
        response = restClient.addService(TEST_NODE_CRITERIA, "192.168.1.1", svc);
        assertThat(response.getStatus(), equalTo(HttpServletResponse.SC_CREATED));

        // Return the effective node
        return restClient.getNode(TEST_NODE_CRITERIA);
    }
}
