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

package org.opennms.features.apilayer.requisition.mappers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

import org.junit.Test;
import org.mapstruct.factory.Mappers;
import org.opennms.integration.api.v1.config.requisition.SnmpPrimaryType;
import org.opennms.integration.api.v1.config.requisition.beans.RequisitionBean;
import org.opennms.integration.api.v1.config.requisition.beans.RequisitionInterfaceBean;
import org.opennms.integration.api.v1.config.requisition.beans.RequisitionNodeBean;
import org.opennms.netmgt.model.PrimaryType;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.opennms.netmgt.provision.persist.requisition.RequisitionAsset;
import org.opennms.netmgt.provision.persist.requisition.RequisitionCategory;
import org.opennms.netmgt.provision.persist.requisition.RequisitionInterface;
import org.opennms.netmgt.provision.persist.requisition.RequisitionMonitoredService;
import org.opennms.netmgt.provision.persist.requisition.RequisitionNode;

public class RequisitionMapperTest {

    private final RequisitionMapper mapper = Mappers.getMapper(RequisitionMapper.class);

    @Test
    public void canMapEmptyRequisitionFromApiToOnms() {
        Requisition onmsRequisition = new Requisition();
        onmsRequisition.setForeignSource("fs");
        onmsRequisition.setDate(new Date(1));

        org.opennms.integration.api.v1.config.requisition.Requisition apiRequisition = RequisitionBean.builder()
                .foreignSource("fs")
                .generatedAt(new Date(1))
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
        // Categories are inserted at the begining of the list, so we need to inverse the order here
        requisitionNode.putCategory(new RequisitionCategory("123"));
        requisitionNode.putCategory(new RequisitionCategory("abc"));

        RequisitionInterface requisitionInterface = new RequisitionInterface();
        requisitionNode.putInterface(requisitionInterface);
        requisitionInterface.setSnmpPrimary(PrimaryType.SECONDARY);
        requisitionInterface.setIpAddr("127.0.0.1");
        requisitionInterface.setDescr("iface descr");
        requisitionInterface.putMonitoredService(new RequisitionMonitoredService("svc1"));

        org.opennms.integration.api.v1.config.requisition.Requisition apiRequisition = org.opennms.integration.api.v1.config.requisition.beans.RequisitionBean.builder()
                .foreignSource("fs")
                .generatedAt(new Date(0))
                .node(RequisitionNodeBean.builder()
                        .nodeLabel("n1")
                        .foreignId("fid")
                        .location("loc")
                        .asset("field", "value")
                        .category("abc")
                        .category("123")
                        .iface(RequisitionInterfaceBean.builder()
                                .ipAddress(InetAddress.getByName("127.0.0.1"))
                                .snmpPrimary(SnmpPrimaryType.SECONDARY)
                                .description("iface descr")
                                .monitoredService("svc1")
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
