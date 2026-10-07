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
package org.opennms.web.rest.v2.mapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.opennms.netmgt.config.threshd.Expression;
import org.opennms.netmgt.config.threshd.FilterOperator;
import org.opennms.netmgt.config.threshd.Group;
import org.opennms.netmgt.config.threshd.ResourceFilter;
import org.opennms.netmgt.config.threshd.Threshold;
import org.opennms.netmgt.config.threshd.ThresholdType;
import org.opennms.web.rest.v2.model.ExpressionDto;
import org.opennms.web.rest.v2.model.ResourceFilterDto;
import org.opennms.web.rest.v2.model.ThresholdDto;
import org.opennms.web.rest.v2.model.ThresholdGroupDto;

public class ThresholdingConfigMapperTest {

    @Test
    public void roundTripsAThresholdWithoutLosingAnything() {
        final ThresholdDto dto = thresholdDto(ThresholdType.HIGH.getEnumName());
        dto.setDescription("High CPU");
        dto.setDsLabel("cpuLabel");
        dto.setTriggeredUEI("uei.opennms.org/example/exceeded");
        dto.setRearmedUEI("uei.opennms.org/example/rearmed");
        dto.setRelaxed(Boolean.TRUE);
        dto.setFilterOperator("and");
        dto.setResourceFilters(List.of(resourceFilterDto("ifDescr", "^eth.*")));

        final ThresholdDto roundTripped = ThresholdingConfigMapper.toDto(ThresholdingConfigMapper.toEntity(dto));

        assertEquals(dto.getDsName(), roundTripped.getDsName());
        assertEquals(dto.getType(), roundTripped.getType());
        assertEquals(dto.getDsType(), roundTripped.getDsType());
        assertEquals(dto.getValue(), roundTripped.getValue());
        assertEquals(dto.getRearm(), roundTripped.getRearm());
        assertEquals(dto.getTrigger(), roundTripped.getTrigger());
        assertEquals(dto.getDescription(), roundTripped.getDescription());
        assertEquals(dto.getDsLabel(), roundTripped.getDsLabel());
        assertEquals(dto.getTriggeredUEI(), roundTripped.getTriggeredUEI());
        assertEquals(dto.getRearmedUEI(), roundTripped.getRearmedUEI());
        assertEquals(Boolean.TRUE, roundTripped.getRelaxed());
        assertEquals("and", roundTripped.getFilterOperator());
        assertEquals(1, roundTripped.getResourceFilters().size());
        assertEquals("ifDescr", roundTripped.getResourceFilters().get(0).getField());
        assertEquals("^eth.*", roundTripped.getResourceFilters().get(0).getContent());
    }

    @Test
    public void dropsTheRearmedUeiForThresholdTypesThatNeverRearm() {
        for (final ThresholdType type : List.of(ThresholdType.RELATIVE_CHANGE, ThresholdType.ABSOLUTE_CHANGE)) {
            final ThresholdDto dto = thresholdDto(type.getEnumName());
            dto.setRearmedUEI("uei.opennms.org/example/rearmed");

            final Threshold entity = ThresholdingConfigMapper.toEntity(dto);

            assertTrue("rearmedUEI should be dropped for " + type, entity.getRearmedUEI().isEmpty());
        }
    }

    @Test
    public void keepsTheRearmedUeiForRearmingAbsoluteChange() {
        final ThresholdDto dto = thresholdDto(ThresholdType.REARMING_ABSOLUTE_CHANGE.getEnumName());
        dto.setRearmedUEI("uei.opennms.org/example/rearmed");

        final Threshold entity = ThresholdingConfigMapper.toEntity(dto);

        assertEquals("uei.opennms.org/example/rearmed", entity.getRearmedUEI().orElse(null));
    }

    @Test
    public void storesEmptyOptionalStringsAsAbsentRatherThanEmpty() {
        final ThresholdDto dto = thresholdDto(ThresholdType.HIGH.getEnumName());
        dto.setDescription("");
        dto.setDsLabel("   ");
        dto.setExprLabel("");
        dto.setTriggeredUEI("");
        dto.setRearmedUEI("");

        final Threshold entity = ThresholdingConfigMapper.toEntity(dto);

        assertTrue(entity.getDescription().isEmpty());
        assertTrue(entity.getDsLabel().isEmpty());
        assertTrue(entity.getExprLabel().isEmpty());
        assertTrue(entity.getTriggeredUEI().isEmpty());
        assertTrue(entity.getRearmedUEI().isEmpty());
    }

    @Test
    public void doesNotWriteRelaxedWhenItIsNotSet() {
        // The getter coerces null to false, so a naive mapper would turn every round-trip into a diff of
        // explicit relaxed="false" attributes against the imported configuration.
        final Threshold entity = ThresholdingConfigMapper.toEntity(thresholdDto(ThresholdType.HIGH.getEnumName()));

        assertFalse(entity.getRelaxed());
        assertNull(readRelaxedField(entity));
    }

    @Test
    public void appliesTheModelDefaultWhenNoFilterOperatorIsGiven() {
        final Threshold entity = ThresholdingConfigMapper.toEntity(thresholdDto(ThresholdType.HIGH.getEnumName()));

        assertEquals(FilterOperator.OR, entity.getFilterOperator());
    }

    @Test
    public void parsesTheFilterOperatorCaseInsensitively() {
        assertEquals(FilterOperator.AND, ThresholdingConfigMapper.toFilterOperator("AND"));
        assertEquals(FilterOperator.OR, ThresholdingConfigMapper.toFilterOperator(" or "));
        assertNull(ThresholdingConfigMapper.toFilterOperator("neither"));
        assertNull(ThresholdingConfigMapper.toFilterOperator(""));
        assertNull(ThresholdingConfigMapper.toFilterOperator(null));
    }

    @Test
    public void roundTripsAGroupWithBothKindsOfDefinition() {
        final ThresholdGroupDto dto = new ThresholdGroupDto();
        dto.setName("mib2");
        dto.setRrdRepository("/opt/opennms/share/rrd/snmp/");
        dto.setThresholds(List.of(thresholdDto(ThresholdType.HIGH.getEnumName())));

        final ExpressionDto expressionDto = new ExpressionDto();
        expressionDto.setExpression("a + b");
        expressionDto.setExprLabel("sum");
        expressionDto.setType(ThresholdType.LOW.getEnumName());
        expressionDto.setDsType("node");
        expressionDto.setValue("1");
        expressionDto.setRearm("2");
        expressionDto.setTrigger("3");
        dto.setExpressions(List.of(expressionDto));

        final Group group = ThresholdingConfigMapper.toEntity(dto);
        final ThresholdGroupDto roundTripped = ThresholdingConfigMapper.toDto(group, false);

        assertEquals("mib2", roundTripped.getName());
        assertEquals("/opt/opennms/share/rrd/snmp/", roundTripped.getRrdRepository());
        assertEquals(1, roundTripped.getThresholds().size());
        assertEquals(1, roundTripped.getExpressions().size());
        assertEquals("a + b", roundTripped.getExpressions().get(0).getExpression());
        assertEquals("sum", roundTripped.getExpressions().get(0).getExprLabel());
        assertEquals(Boolean.FALSE, roundTripped.getReadOnly());
    }

    @Test
    public void summarizesAGroupByCountingItsDefinitions() {
        final Group group = new Group();
        group.setName("mib2");
        group.setRrdRepository("/rrd");
        group.addThreshold(new Threshold());
        group.addThreshold(new Threshold());
        group.addExpression(new Expression());

        final var summary = ThresholdingConfigMapper.toSummaryDto(group, true);

        assertEquals(Integer.valueOf(2), summary.getThresholdCount());
        assertEquals(Integer.valueOf(1), summary.getExpressionCount());
        assertEquals(Boolean.TRUE, summary.getReadOnly());
    }

    @Test
    public void unwrapsAnAbsentResourceFilterBodyToNull() {
        final ResourceFilter filter = new ResourceFilter();
        filter.setField("ifDescr");

        assertNull(ThresholdingConfigMapper.toDto(filter).getContent());
    }

    private static ThresholdDto thresholdDto(final String type) {
        final ThresholdDto dto = new ThresholdDto();
        dto.setDsName("cpuUtilization");
        dto.setType(type);
        dto.setDsType("node");
        dto.setValue("90");
        dto.setRearm("70");
        dto.setTrigger("3");
        return dto;
    }

    private static ResourceFilterDto resourceFilterDto(final String field, final String content) {
        final ResourceFilterDto dto = new ResourceFilterDto();
        dto.setField(field);
        dto.setContent(content);
        return dto;
    }

    /** The getter hides whether the attribute is actually set, so read the field to tell the two apart. */
    private static Object readRelaxedField(final Threshold threshold) {
        try {
            final var field = threshold.getClass().getSuperclass().getDeclaredField("m_relaxed");
            field.setAccessible(true);
            return field.get(threshold);
        } catch (final ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }
}
