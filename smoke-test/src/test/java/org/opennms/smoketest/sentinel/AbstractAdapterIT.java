/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018-2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.smoketest.sentinel;

import static com.jayway.awaitility.Awaitility.await;
import static io.restassured.RestAssured.preemptive;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.hibernate.NodeDaoHibernate;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.PrimaryType;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.opennms.netmgt.provision.persist.requisition.RequisitionInterface;
import org.opennms.netmgt.provision.persist.requisition.RequisitionMetaData;
import org.opennms.netmgt.provision.persist.requisition.RequisitionNode;
import org.opennms.smoketest.containers.OpenNMSContainer;
import org.opennms.smoketest.stacks.OpenNMSStack;
import org.opennms.smoketest.stacks.MinionProfile;
import org.opennms.smoketest.stacks.StackModel;
import org.opennms.smoketest.utils.DaoUtils;
import org.opennms.smoketest.utils.HibernateDaoFactory;
import org.opennms.smoketest.utils.KarafShell;
import org.opennms.smoketest.utils.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

/**
 * Consolidates all non flow telemetry adapter tests
 */
public abstract class AbstractAdapterIT {

    // Helper Object to create a requisition from
    protected static class RequisitionCreateInfo {
        protected String location = MinionProfile.DEFAULT_LOCATION;
        protected String nodeLabel;
        protected String foreignId;
        protected String foreignSource;
        protected String ipAddress;
        protected List<RequisitionMetaData> metaData;

        public Requisition createRequisition() {
            Objects.requireNonNull(location);
            Objects.requireNonNull(nodeLabel);
            Objects.requireNonNull(foreignId);
            Objects.requireNonNull(foreignSource);
            Objects.requireNonNull(ipAddress);

            final Requisition requisition = new Requisition(foreignSource);
            final List<RequisitionInterface> interfaces = new ArrayList<>();
            final RequisitionInterface requisitionInterface = new RequisitionInterface();
            requisitionInterface.setIpAddr(ipAddress);
            requisitionInterface.setManaged(true);
            requisitionInterface.setSnmpPrimary(PrimaryType.PRIMARY);
            interfaces.add(requisitionInterface);

            final RequisitionNode node = new RequisitionNode();
            node.setNodeLabel(nodeLabel);
            node.setForeignId(foreignId);
            node.setLocation(location);
            node.setInterfaces(interfaces);
            node.setMetaData(metaData != null ? metaData : Collections.emptyList());
            requisition.insertNode(node);

            return requisition;
        }
    }

    @Rule
    public Timeout timeout = new Timeout(20, TimeUnit.MINUTES);

    @ClassRule
    public static final OpenNMSStack stack = OpenNMSStack.withModel(StackModel.newBuilder()
            .withMinion()
            .withSentinel()
            .withTelemetryProcessing()
            .build());

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Test
    public void verifyAdapter() throws Exception {
        // Determine endpoints
        final InetSocketAddress sentinelSshAddress = stack.sentinel().getSshAddress();
        final InetSocketAddress opennmsHttpAddress = stack.opennms().getWebAddress();

        // Configure RestAssured
        RestAssured.baseURI = String.format("http://%s:%s/opennms", opennmsHttpAddress.getHostName(), opennmsHttpAddress.getPort());
        RestAssured.port = opennmsHttpAddress.getPort();
        RestAssured.basePath = "/rest";
        RestAssured.authentication = preemptive().basic(OpenNMSContainer.ADMIN_USER, OpenNMSContainer.ADMIN_PASSWORD);

        // The package send may contain a node, which must be created in order to have the adapter store it to newts
        // so we check if this is the case and afterwards create the requisition
        final RequisitionCreateInfo requisitionToCreate = getRequisitionToCreate();
        if (requisitionToCreate != null) {
            createRequisition(requisitionToCreate, opennmsHttpAddress, stack.postgres().getDaoFactory());
        }

        // Wait until a route for procession is actually started
        new KarafShell(sentinelSshAddress).verifyLog(getSentinelReadyVerificationFunction());

        // If a new requisition was created, also probably new nodes are available.
        // However, sentinel may not know about it yet, so we manually sync the InterfaceToNodeCache in order to
        // "see" the new nodes and interfaces.
        if (requisitionToCreate != null) {
            new KarafShell(sentinelSshAddress).runCommand("nodecache:sync");
        }

        // Resource Id to verify against
        final String resourceId = getResourceId();

        // Ensure no measurement data available
        final Response response = RestAssured.given().accept(ContentType.JSON)
                .get("/measurements/" + resourceId);
        Assert.assertEquals(404, response.statusCode());

        await().atMost(3, TimeUnit.MINUTES).pollInterval(10, TimeUnit.SECONDS).until(
                () -> {
                    // Send packet to Minion
                    sendTelemetryMessage();
                    // Verify that the resource exists
                    final Response theResponse = RestAssured.given().accept(ContentType.JSON)
                            .get("/measurements/" + resourceId);
                    return theResponse.statusCode() == 200;
                }
        );
    }

    // Hook to allow tests to send custom messages to minion
    protected abstract void sendTelemetryMessage() throws IOException;

    // The resource id to check for the test
    protected abstract String getResourceId();

    // A function to parse the log output of sentinel in order to verify if sentinel is ready (e.g. adapter has been started)
    protected abstract Function<String,Boolean> getSentinelReadyVerificationFunction();

    // Some tests require a requisition, if provided it will be created
    protected abstract RequisitionCreateInfo getRequisitionToCreate();

    // Creates the requisition
    static OnmsNode createRequisition(RequisitionCreateInfo createInfo, InetSocketAddress opennmsHttp, HibernateDaoFactory daoFactory) {
        Objects.requireNonNull(createInfo);
        Objects.requireNonNull(opennmsHttp);
        Objects.requireNonNull(daoFactory);

        // Create Requisition
        final RestClient client = new RestClient(opennmsHttp);
        final Requisition requisition = createInfo.createRequisition();
        client.addOrReplaceRequisition(requisition);
        client.importRequisition(requisition.getForeignSource());

        // Verify creation
        final NodeDao nodeDao = daoFactory.getDao(NodeDaoHibernate.class);
        final OnmsNode onmsNode = await().atMost(3, MINUTES).pollInterval(30, SECONDS)
                .until(DaoUtils.findMatchingCallable(nodeDao, new CriteriaBuilder(OnmsNode.class)
                        .eq("foreignSource", createInfo.foreignSource)
                        .eq("foreignId", createInfo.foreignId)
                        .eq("label", createInfo.nodeLabel).toCriteria()), notNullValue());

        assertNotNull(onmsNode);

        return onmsNode;

    }

}
