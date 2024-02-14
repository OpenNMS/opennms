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
package org.opennms.features.apilayer.config;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Test;
import org.opennms.integration.api.v1.config.thresholding.Basethresholddef;
import org.opennms.integration.api.v1.config.thresholding.Expression;
import org.opennms.integration.api.v1.config.thresholding.FilterOperator;
import org.opennms.integration.api.v1.config.thresholding.GroupDefinition;
import org.opennms.integration.api.v1.config.thresholding.ResourceFilter;
import org.opennms.integration.api.v1.config.thresholding.Threshold;
import org.opennms.integration.api.v1.config.thresholding.ThresholdType;
import org.opennms.integration.api.v1.config.thresholding.ThresholdingConfigExtension;
import org.opennms.netmgt.config.dao.thresholding.api.WriteableThresholdingDao;
import org.opennms.netmgt.config.threshd.Group;
import org.opennms.netmgt.config.threshd.ThresholdingConfig;
import org.opennms.netmgt.threshd.api.ThresholdingService;
import org.opennms.netmgt.threshd.api.ThresholdingSetPersister;

public class ThresholdingConfigExtensionManagerTest {
    private static final String description = "description";
    private static final String dsLabel = "ds label";
    private static final String dsType = "ds type";
    private static final FilterOperator filterOperator = FilterOperator.AND;
    private static final String rearm = "1.0";
    private static final String rearmedUEI = "rearmed UEI";
    private static final boolean relaxed = true;
    private static final ResourceFilter resourceFilter = mock(ResourceFilter.class);
    private static final String content = "content";
    private static final String field = "field";
    private static final String trigger = "2";
    private static final String triggeredUEI = "triggered uei";
    private static final String value = "3.0";
    private static final ThresholdType type = ThresholdType.HIGH;

    /**
     * Tests that mapping from API type to OpenNMS type works and multiple configurations provided by extensions are
     * merged together.
     */
    @Test
    public void canProvideMergedConfiguration() {
        WriteableThresholdingDao thresholdingDao = mock(WriteableThresholdingDao.class);
        ThresholdingService thresholdingService = mock(ThresholdingService.class);
        when(thresholdingService.getThresholdingSetPersister()).thenReturn(mock(ThresholdingSetPersister.class));
        ThresholdingConfigExtensionManager manager = new ThresholdingConfigExtensionManager(thresholdingDao,
                thresholdingService);

        // Shouldn't have config yet
        ThresholdingConfig thresholdingConfig = manager.getObject();
        assertThat(thresholdingConfig.getGroups(), hasSize(0));

        // Expose an extension
        ThresholdingConfigExtension ext1 = mock(ThresholdingConfigExtension.class);
        GroupDefinition groupDef = mock(GroupDefinition.class);

        String groupName = "group name";
        when(groupDef.getName()).thenReturn(groupName);
        String rrdRepository = "rrd repository";
        when(groupDef.getRrdRepository()).thenReturn(rrdRepository);

        Expression expression = mock(Expression.class);
        String expressionStr = "expression";
        when(expression.getExpression()).thenReturn(expressionStr);
        mockBaseThresholddef(expression);
        when(groupDef.getExpressions()).thenReturn(Collections.singletonList(expression));

        Threshold threshold = mock(Threshold.class);
        String dsName = "ds name";
        when(threshold.getDsName()).thenReturn(dsName);
        mockBaseThresholddef(threshold);
        when(groupDef.getThresholds()).thenReturn(Collections.singletonList(threshold));

        when(ext1.getGroupDefinitions()).thenReturn(Collections.singletonList(groupDef));

        manager.onBind(ext1, Collections.emptyMap());

        // Should be configuration now
        thresholdingConfig = manager.getObject();
        assertThat(thresholdingConfig.getGroups(), hasSize(1));
        Group extGroup = thresholdingConfig.getGroups().get(0);
        assertThat(extGroup.getName(), equalTo(groupName));
        assertThat(extGroup.getRrdRepository(), equalTo(rrdRepository));

        org.opennms.netmgt.config.threshd.Expression extExpression = extGroup.getExpressions().get(0);
        assertThat(extExpression.getExpression(), equalTo(expressionStr));
        checkBasethresholddef(extExpression);

        org.opennms.netmgt.config.threshd.Threshold extThreshold = extGroup.getThresholds().get(0);
        assertThat(extThreshold.getDsName(), equalTo(dsName));
        checkBasethresholddef(extThreshold);

        // Expose another extension
        ThresholdingConfigExtension ext2 = mock(ThresholdingConfigExtension.class);
        GroupDefinition groupDef2 = mock(GroupDefinition.class);
        String groupName2 = "new group name";
        when(groupDef2.getName()).thenReturn(groupName2);
        String rrdRepository2 = "rrd repository 2";
        when(groupDef2.getRrdRepository()).thenReturn(rrdRepository2);
        when(groupDef2.getThresholds()).thenReturn(Collections.emptyList());
        when(groupDef2.getExpressions()).thenReturn(Collections.emptyList());
        when(ext2.getGroupDefinitions()).thenReturn(Collections.singletonList(groupDef2));
        manager.onBind(ext2, Collections.emptyMap());

        // Should see the merged results now
        thresholdingConfig = manager.getObject();
        List<String> groupNames = thresholdingConfig.getGroups()
                .stream()
                .map(Group::getName)
                .collect(Collectors.toList());
        assertThat(groupNames, hasItems(groupName, groupName2));

        // Unbind ext2
        manager.onUnbind(ext2, Collections.emptyMap());
        thresholdingConfig = manager.getObject();
        assertThat(thresholdingConfig.getGroups(), hasSize(1));

        // Unbind ext1
        manager.onUnbind(ext1, Collections.emptyMap());
        thresholdingConfig = manager.getObject();
        assertThat(thresholdingConfig.getGroups(), hasSize(0));
    }

    private static void mockBaseThresholddef(Basethresholddef basethresholddef) {
        when(basethresholddef.getDescription()).thenReturn(Optional.of(description));
        when(basethresholddef.getDsLabel()).thenReturn(Optional.of(dsLabel));
        when(basethresholddef.getDsType()).thenReturn(dsType);
        when(basethresholddef.getFilterOperator()).thenReturn(filterOperator);
        when(basethresholddef.getRearm()).thenReturn(rearm);
        when(basethresholddef.getRearmedUEI()).thenReturn(Optional.of(rearmedUEI));
        when(basethresholddef.getRelaxed()).thenReturn(relaxed);
        ResourceFilter resourceFilter = mock(ResourceFilter.class);
        when(resourceFilter.getContent()).thenReturn(Optional.of(content));
        when(resourceFilter.getField()).thenReturn(field);
        when(basethresholddef.getResourceFilters()).thenReturn(Collections.singletonList(resourceFilter));
        when(basethresholddef.getTrigger()).thenReturn(trigger);
        when(basethresholddef.getTriggeredUEI()).thenReturn(Optional.of(triggeredUEI));
        when(basethresholddef.getType()).thenReturn(type);
        when(basethresholddef.getValue()).thenReturn(value);
    }

    private static void checkBasethresholddef(org.opennms.netmgt.config.threshd.Basethresholddef basethresholddef) {
        assertThat(basethresholddef.getDescription().get(), equalTo(description));
        assertThat(basethresholddef.getDsType(), equalTo(dsType));
        assertThat(basethresholddef.getDsLabel().get(), equalTo(dsLabel));
        assertThat(basethresholddef.getFilterOperator().name(), equalTo(filterOperator.name()));
        assertThat(basethresholddef.getRearm(), equalTo(rearm));
        assertThat(basethresholddef.getRearmedUEI().get(), equalTo(rearmedUEI));
        assertThat(basethresholddef.getRelaxed(), equalTo(relaxed));
        assertThat(basethresholddef.getResourceFilters().get(0).getContent().get(), equalTo(content));
        assertThat(basethresholddef.getResourceFilters().get(0).getField(), equalTo(field));
        assertThat(basethresholddef.getTrigger(), equalTo(trigger));
        assertThat(basethresholddef.getTriggeredUEI().get(), equalTo(triggeredUEI));
        assertThat(basethresholddef.getType().name(), equalTo(type.name()));
        assertThat(basethresholddef.getValue(), equalTo(value));
    }
}
