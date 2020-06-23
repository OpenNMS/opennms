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

package org.opennms.features.topology.plugins.topo.asset;

import org.junit.Assert;
import org.junit.Test;
import org.opennms.features.topology.api.support.breadcrumbs.BreadcrumbStrategy;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;

import com.google.common.collect.Lists;

public class GeneratorConfigBuilderTest {

    @Test
    public void verifyBuildEvent() {
        final Event e = new EventBuilder(AssetGraphMLProvider.CREATE_ASSET_TOPOLOGY, getClass().getSimpleName())
                .addParam(EventParameterNames.PROVIDER_ID, "My Id")
                .addParam(EventParameterNames.LABEL, "Custom Asset Topology")
                .addParam(EventParameterNames.PREFERRED_LAYOUT, "D3 Layout")
                .addParam(EventParameterNames.BREADCRUMB_STRATEGY, "NONE")
                .addParam(EventParameterNames.FILTERS, "a=x;b=y")
                .addParam(EventParameterNames.HIERARCHY, "a,b,c")
                .getEvent();

        final GeneratorConfig expectedConfig = new GeneratorConfig();
        expectedConfig.setProviderId("My Id");
        expectedConfig.setLabel("Custom Asset Topology");
        expectedConfig.setPreferredLayout("D3 Layout");
        expectedConfig.setBreadcrumbStrategy(BreadcrumbStrategy.NONE.name());
        expectedConfig.setLayerHierarchies(Lists.newArrayList("a", "b", "c"));
        expectedConfig.setFilters(Lists.newArrayList("a=x","b=y"));

        final GeneratorConfig actualConfig = GeneratorConfigBuilder.buildFrom(e);
        Assert.assertEquals(expectedConfig, actualConfig);
    }
}