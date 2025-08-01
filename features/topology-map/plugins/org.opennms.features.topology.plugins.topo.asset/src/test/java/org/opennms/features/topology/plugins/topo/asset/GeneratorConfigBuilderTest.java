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
package org.opennms.features.topology.plugins.topo.asset;

import org.junit.Assert;
import org.junit.Test;
import org.opennms.features.topology.api.support.breadcrumbs.BreadcrumbStrategy;
import org.opennms.netmgt.events.api.model.ImmutableMapper;
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

        final GeneratorConfig actualConfig = GeneratorConfigBuilder.buildFrom(ImmutableMapper.fromMutableEvent(e));
        Assert.assertEquals(expectedConfig, actualConfig);
    }
}