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

package org.opennms.netmgt.provision.persist.requisition;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.model.requisition.RequisitionEntity;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-mockDao.xml",
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/applicationContext-requisitionMerger.xml"
})
@JUnitConfigurationEnvironment
public class DefaultRequisitionMergerIT {

    @Autowired
    private RequisitionMerger requisitionMerger;

    @Test
    public void testMerge() {
        final String requisitionXML = "<model-import foreign-source=\"" + getClass().getSimpleName() + "\">" +
                "   <node foreign-id=\"NodeA\" node-label=\"NodeA\">" +
                "       <interface ip-addr=\"::1\" status=\"1\" snmp-primary=\"N\">" +
                "           <monitored-service service-name=\"AAA\"/>" +
                "           <monitored-service service-name=\"BBB\"/>" +
                "       </interface>" +
                "       <interface ip-addr=\"127.0.0.1\" status=\"1\" snmp-primary=\"N\">" +
                "           <monitored-service service-name=\"CCC\"/>" +
                "           <monitored-service service-name=\"DDD\"/>" +
                "       </interface>" +
                "   </node>" +
                "</model-import>";

        final Requisition requisition = JaxbUtils.unmarshal(Requisition.class, requisitionXML);
        Assert.assertNotNull(requisition);
        Assert.assertEquals(1, requisition.getNodes().size());
        Assert.assertEquals(2, requisition.getNodes().get(0).getInterfaces().size());
        requisition.getNodes().get(0).getInterfaces().forEach(ipInterface -> Assert.assertEquals(2, ipInterface.getMonitoredServiceCount()));

        final RequisitionEntity requisitionEntity = requisitionMerger.mergeOrCreate(requisition);
        Assert.assertNotNull(requisitionEntity);
        Assert.assertEquals(1, requisitionEntity.getNodes().size());
        Assert.assertEquals(2, requisitionEntity.getNodes().get(0).getInterfaces().size());
        requisitionEntity.getNodes().get(0).getInterfaces().forEach(ipInterface -> Assert.assertEquals(2, ipInterface.getMonitoredServices().size()));

    }

}