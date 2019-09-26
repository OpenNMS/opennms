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
        final List<AdapterDefinition> adapters = new AdapterDefinitionParser().parse(properties);
        Assert.assertEquals(2, adapters.size());
        Assert.assertThat(adapters, CoreMatchers.hasItems(
                new MapBasedAdapterDef(
                        ImmutableMap.of("name", "SFlow-Parser",
                                "class-name", "org.opennms.netmgt.telemetry.protocols.sflow.adapter.SFlowAdapter")
                ),
                new MapBasedAdapterDef(
                        ImmutableMap.of("name", "SFLOW-Telemetry",
                                "class-name", "org.opennms.netmgt.telemetry.protocols.sflow.adapter.SFlowTelemetryAdapter",
                                "parameters.script", "/opt/sentinel/etc/sflow-host.groovy")
                )
        ));
    }

    @Test
    public void verifySingleAdapter() {
        final Map<String, String> properties = new HashMap();
        properties.put("name", "Netflow-5");
        properties.put("adapters.1.name", "Netflow-5-Parser");
        properties.put("adapters.1.class-name", "org.opennms.netmgt.telemetry.protocols.netflow.adapter.netflow5.Netflow5Adapter");

        final List<AdapterDefinition> adapters = new AdapterDefinitionParser().parse(properties);
        Assert.assertEquals(1, adapters.size());
    }

    @Test
    public void verifyLegacyDefinition() {
        final Map<String, String> properties = new HashMap();
        properties.put("name", "Netflow-5");
        properties.put("class-name", "org.opennms.netmgt.telemetry.protocols.netflow.adapter.netflow5.Netflow5Adapter");

        final List<AdapterDefinition> adapters = new AdapterDefinitionParser().parse(properties);
        Assert.assertEquals(1, adapters.size());
    }

}