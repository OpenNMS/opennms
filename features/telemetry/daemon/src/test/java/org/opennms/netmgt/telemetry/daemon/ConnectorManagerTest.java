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
