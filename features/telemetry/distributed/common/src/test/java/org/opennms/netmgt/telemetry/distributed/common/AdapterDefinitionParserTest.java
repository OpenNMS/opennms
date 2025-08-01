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
package org.opennms.netmgt.telemetry.distributed.common;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.opennms.netmgt.telemetry.config.api.AdapterDefinition;

import com.google.common.collect.ImmutableMap;

public class AdapterDefinitionParserTest {

    @Test
    public void verifyMultipleAdapters() {
        // Create configuration, as it would be in any *.cfg file
        final Map<String, String> properties = new HashMap();
        properties.put("name", "SFlow");
        properties.put("adapters.1.name", "SFlow-Parser");
        properties.put("adapters.1.class-name", "org.opennms.netmgt.telemetry.protocols.sflow.adapter.SFlowAdapter");
        properties.put("adapters.2.name", "SFLOW-Telemetry");
        properties.put("adapters.2.class-name", "org.opennms.netmgt.telemetry.protocols.sflow.adapter.SFlowTelemetryAdapter");
        properties.put("adapters.2.parameters.script", "/opt/sentinel/etc/sflow-host.groovy");

        // Parse and verify
        final List<AdapterDefinition> adapters = new AdapterDefinitionParser().parse("Test", PropertyTree.from(properties));
        Assert.assertEquals(2, adapters.size());
        Assert.assertThat(adapters, CoreMatchers.hasItems(
                new MapBasedAdapterDef(
                        "Test",
                        PropertyTree.from(ImmutableMap.of("name", "SFlow-Parser",
                                "class-name", "org.opennms.netmgt.telemetry.protocols.sflow.adapter.SFlowAdapter"))
                ),
                new MapBasedAdapterDef(
                        "Test",
                        PropertyTree.from(ImmutableMap.of("name", "SFLOW-Telemetry",
                                "class-name", "org.opennms.netmgt.telemetry.protocols.sflow.adapter.SFlowTelemetryAdapter",
                                "parameters.script", "/opt/sentinel/etc/sflow-host.groovy"))
                )
        ));
    }

    @Test
    public void verifySingleAdapter() {
        final Map<String, String> properties = new HashMap();
        properties.put("name", "Netflow-5");
        properties.put("adapters.1.name", "Netflow-5-Parser");
        properties.put("adapters.1.class-name", "org.opennms.netmgt.telemetry.protocols.netflow.adapter.netflow5.Netflow5Adapter");

        final List<AdapterDefinition> adapters = new AdapterDefinitionParser().parse("Test", PropertyTree.from(properties));
        Assert.assertEquals(1, adapters.size());
    }

    @Test
    public void verifyLegacyDefinition() {
        final Map<String, String> properties = new HashMap();
        properties.put("name", "Netflow-5");
        properties.put("class-name", "org.opennms.netmgt.telemetry.protocols.netflow.adapter.netflow5.Netflow5Adapter");

        final List<AdapterDefinition> adapters = new AdapterDefinitionParser().parse("Test", PropertyTree.from(properties));
        Assert.assertEquals(1, adapters.size());
    }

    /**
     * see NMS-13477
     */
    @Test
    public void testWhitespaces() {
        final Map<String, String> properties = new HashMap();
        properties.put("name", " SFlow");
        properties.put("adapters.1.name", " SFlow-Parser");
        properties.put("adapters.1.class-name", " org.opennms.netmgt.telemetry.protocols.sflow.adapter.SFlowAdapter");
        properties.put("adapters.2.name", "SFLOW-Telemetry ");
        properties.put("adapters.2.class-name", "org.opennms.netmgt.telemetry.protocols.sflow.adapter.SFlowTelemetryAdapter ");
        properties.put("adapters.2.parameters.script", "/opt/sentinel/etc/sflow-host.groovy ");

        // Parse and verify
        final List<AdapterDefinition> adapters = new AdapterDefinitionParser().parse("Test", PropertyTree.from(properties));
        Assert.assertEquals(2, adapters.size());
        Assert.assertThat(adapters, CoreMatchers.hasItems(
                new MapBasedAdapterDef(
                        "Test",
                        PropertyTree.from(ImmutableMap.of("name", "SFlow-Parser",
                                "class-name", "org.opennms.netmgt.telemetry.protocols.sflow.adapter.SFlowAdapter"))
                ),
                new MapBasedAdapterDef(
                        "Test",
                        PropertyTree.from(ImmutableMap.of("name", "SFLOW-Telemetry",
                                "class-name", "org.opennms.netmgt.telemetry.protocols.sflow.adapter.SFlowTelemetryAdapter",
                                "parameters.script", "/opt/sentinel/etc/sflow-host.groovy"))
                )
        ));
    }
}
