/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.daemon;

import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.opennms.core.rpc.mock.MockEntityScopeProvider;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.api.ServiceRef;
import org.opennms.netmgt.telemetry.config.model.PackageConfig;
import org.opennms.netmgt.telemetry.config.model.Parameter;

public class ConnectorManagerTest {


    @Test
    public void testParamsByGroup() {

        PackageConfig connectorPackage = new PackageConfig();
        connectorPackage.getParameters().add(new Parameter("port", "50052"));
        connectorPackage.getParameters().add(new Parameter("group1","paths", "/interfaces"));
        connectorPackage.getParameters().add(new Parameter("group1", "frequency", "5000"));
        connectorPackage.getParameters().add(new Parameter("group2", "frequency", "3000"));
        connectorPackage.getParameters().add(new Parameter("group2", "paths",
                "/network-instances/network-instance[instance-name='master']"));
        connectorPackage.getParameters().add(new Parameter("group3", "paths", "/protocols/protocol/bgp"));
        connectorPackage.getParameters().add(new Parameter("group3", "frequency", "4000"));

        ConnectorManager connectorManager = new ConnectorManager();
        connectorManager.setEntityScopeProvider(new MockEntityScopeProvider());
        ServiceRef serviceRef = new ServiceRef(1, InetAddressUtils.ONE_TWENTY_SEVEN, "OPENCONFIG");
        List<Map<String, String>> groupedParams = connectorManager.getGroupedParams(connectorPackage, serviceRef);
        Assert.assertEquals(4, groupedParams.size());
        // Each map should either belongs to different group which has paths or it should be a group with port ( which is global)
        for(Map<String, String> parms : groupedParams) {
            Assert.assertTrue(parms.containsKey("paths") || parms.containsKey("port"));
        }

    }
}
