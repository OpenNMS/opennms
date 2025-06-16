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
package org.opennms.features.apilayer.common.requisition.mappers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Date;

import org.junit.Test;
import org.mapstruct.factory.Mappers;
import org.opennms.integration.api.v1.config.requisition.SnmpPrimaryType;
import org.opennms.integration.api.v1.config.requisition.immutables.ImmutableRequisition;
import org.opennms.integration.api.v1.config.requisition.immutables.ImmutableRequisitionInterface;
import org.opennms.integration.api.v1.config.requisition.immutables.ImmutableRequisitionMetaData;
import org.opennms.integration.api.v1.config.requisition.immutables.ImmutableRequisitionMonitoredService;
import org.opennms.integration.api.v1.config.requisition.immutables.ImmutableRequisitionNode;
import org.opennms.netmgt.model.PrimaryType;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.opennms.netmgt.provision.persist.requisition.RequisitionAsset;
import org.opennms.netmgt.provision.persist.requisition.RequisitionCategory;
import org.opennms.netmgt.provision.persist.requisition.RequisitionInterface;
import org.opennms.netmgt.provision.persist.requisition.RequisitionMetaData;
import org.opennms.netmgt.provision.persist.requisition.RequisitionMonitoredService;
import org.opennms.netmgt.provision.persist.requisition.RequisitionNode;

public class RequisitionMapperTest {

    private final RequisitionMapper mapper = Mappers.getMapper(RequisitionMapper.class);

    @Test
    public void canMapEmptyRequisitionFromApiToOnms() {
        Requisition onmsRequisition = new Requisition();
        onmsRequisition.setForeignSource("fs");
        onmsRequisition.setDate(new Date(1));

        org.opennms.integration.api.v1.config.requisition.Requisition apiRequisition = ImmutableRequisition.newBuilder()
                .setForeignSource("fs")
                .setGeneratedAt(new Date(1))
                .build();

        mapAndVerify(onmsRequisition, apiRequisition);
    }

    @Test
    public void canMapCompleteRequisition() throws UnknownHostException {
        Requisition onmsRequisition = new Requisition();
        onmsRequisition.setForeignSource("fs");
        onmsRequisition.setDate(new Date(0));

        RequisitionNode requisitionNode = new RequisitionNode();
        onmsRequisition.insertNode(requisitionNode);
        requisitionNode.setNodeLabel("n1");
        requisitionNode.setForeignId("fid");
        requisitionNode.setLocation("loc");
        requisitionNode.putAsset(new RequisitionAsset("field", "value"));
        // Categories are inserted at the beginning of the list, so we need to inverse the order here
        requisitionNode.putCategory(new RequisitionCategory("123"));
        requisitionNode.putCategory(new RequisitionCategory("abc"));
        requisitionNode.setMetaData(Arrays.asList(
                new RequisitionMetaData("ctx1", "k1", "nv1"),
                new RequisitionMetaData("ctx2", "k2", "nv2")
        ));

        RequisitionInterface requisitionInterface = new RequisitionInterface();
        requisitionNode.putInterface(requisitionInterface);
        requisitionInterface.setSnmpPrimary(PrimaryType.SECONDARY);
        requisitionInterface.setIpAddr("127.0.0.1");
        requisitionInterface.setDescr("iface descr");
        requisitionInterface.setMetaData(Arrays.asList(
                new RequisitionMetaData("ctx1", "k1", "iv1"),
                new RequisitionMetaData("ctx2", "k2", "iv2")
        ));

        RequisitionMonitoredService requisitionService = new RequisitionMonitoredService("svc1");
        requisitionInterface.putMonitoredService(requisitionService);
        requisitionService.setMetaData(Arrays.asList(
                new RequisitionMetaData("ctx1", "k1", "sv1"),
                new RequisitionMetaData("ctx2", "k2", "sv2")
        ));

        org.opennms.integration.api.v1.config.requisition.Requisition apiRequisition = ImmutableRequisition.newBuilder()
                .setForeignSource("fs")
                .setGeneratedAt(new Date(0))
                .addNode(ImmutableRequisitionNode.newBuilder()
                        .setNodeLabel("n1")
                        .setForeignId("fid")
                        .setLocation("loc")
                        .addAsset("field", "value")
                        .addCategory("abc")
                        .addCategory("123")
                        .addMetaData(ImmutableRequisitionMetaData.newInstance("ctx1", "k1", "nv1"))
                        .addMetaData(ImmutableRequisitionMetaData.newInstance("ctx2", "k2", "nv2"))
                        .addInterface(ImmutableRequisitionInterface.newBuilder()
                                .setIpAddress(InetAddress.getByName("127.0.0.1"))
                                .setSnmpPrimary(SnmpPrimaryType.SECONDARY)
                                .setDescription("iface descr")
                                .addMetaData(ImmutableRequisitionMetaData.newInstance("ctx1", "k1", "iv1"))
                                .addMetaData(ImmutableRequisitionMetaData.newInstance("ctx2", "k2", "iv2"))
                                .addMonitoredService(ImmutableRequisitionMonitoredService.newBuilder()
                                        .setName("svc1")
                                        .addMetaData(ImmutableRequisitionMetaData.newInstance("ctx1", "k1", "sv1"))
                                        .addMetaData(ImmutableRequisitionMetaData.newInstance("ctx2", "k2", "sv2"))
                                        .build())
                                .build())
                        .build())
                .build();

        mapAndVerify(onmsRequisition, apiRequisition);
    }

    private void mapAndVerify(Requisition onmsRequsition, org.opennms.integration.api.v1.config.requisition.Requisition apiRequisition) {
        // From ONMS to API
        org.opennms.integration.api.v1.config.requisition.Requisition mappedApiRequisition = mapper.map(onmsRequsition);
        assertThat(mappedApiRequisition, equalTo(apiRequisition));
        // From API to ONMS
        Requisition mappedOnmsRequisition = mapper.map(apiRequisition);
        assertThat(mappedOnmsRequisition, equalTo(onmsRequsition));
    }
}
