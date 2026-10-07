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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.opennms.netmgt.config.threshd.Basethresholddef;
import org.opennms.netmgt.config.threshd.Expression;
import org.opennms.netmgt.config.threshd.FilterOperator;
import org.opennms.netmgt.config.threshd.Group;
import org.opennms.netmgt.config.threshd.ResourceFilter;
import org.opennms.netmgt.config.threshd.Threshold;
import org.opennms.netmgt.config.threshd.ThresholdType;
import org.opennms.web.rest.v2.model.BaseThresholdDefDto;
import org.opennms.web.rest.v2.model.ExpressionDto;
import org.opennms.web.rest.v2.model.ResourceFilterDto;
import org.opennms.web.rest.v2.model.ThresholdDto;
import org.opennms.web.rest.v2.model.ThresholdGroupDto;
import org.opennms.web.rest.v2.model.ThresholdGroupSummaryDto;

/**
 * Converts between the JAXB thresholding config model and the REST DTOs.
 *
 * <p>Two conversions carry behaviour rather than plumbing, both inherited from the JSP editor this API
 * replaces: an empty string on an optional field is stored as null, and {@code rearmedUEI} is dropped for
 * threshold types that never re-arm.</p>
 */
public abstract class ThresholdingConfigMapper {

    private ThresholdingConfigMapper() {
    }

    // ------------------------------------------------------------------ to DTO

    public static ThresholdGroupSummaryDto toSummaryDto(final Group group, final boolean readOnly) {
        final ThresholdGroupSummaryDto dto = new ThresholdGroupSummaryDto();
        dto.setName(group.getName());
        dto.setRrdRepository(group.getRrdRepository());
        dto.setThresholdCount(group.getThresholds().size());
        dto.setExpressionCount(group.getExpressions().size());
        dto.setReadOnly(readOnly);
        return dto;
    }

    public static ThresholdGroupDto toDto(final Group group, final boolean readOnly) {
        final ThresholdGroupDto dto = new ThresholdGroupDto();
        dto.setName(group.getName());
        dto.setRrdRepository(group.getRrdRepository());
        dto.setThresholds(group.getThresholds().stream()
                .map(ThresholdingConfigMapper::toDto)
                .collect(Collectors.toList()));
        dto.setExpressions(group.getExpressions().stream()
                .map(ThresholdingConfigMapper::toDto)
                .collect(Collectors.toList()));
        dto.setReadOnly(readOnly);
        return dto;
    }

    public static ThresholdDto toDto(final Threshold threshold) {
        final ThresholdDto dto = new ThresholdDto();
        copyToDto(threshold, dto);
        dto.setDsName(threshold.getDsName());
        return dto;
    }

    public static ExpressionDto toDto(final Expression expression) {
        final ExpressionDto dto = new ExpressionDto();
        copyToDto(expression, dto);
        dto.setExpression(expression.getExpression());
        return dto;
    }

    private static void copyToDto(final Basethresholddef def, final BaseThresholdDefDto dto) {
        dto.setRelaxed(def.getRelaxed());
        dto.setDescription(def.getDescription().orElse(null));
        dto.setType(def.getType() == null ? null : def.getType().getEnumName());
        dto.setDsType(def.getDsType());
        dto.setValue(def.getValue());
        dto.setRearm(def.getRearm());
        dto.setTrigger(def.getTrigger());
        dto.setDsLabel(def.getDsLabel().orElse(null));
        dto.setExprLabel(def.getExprLabel().orElse(null));
        dto.setTriggeredUEI(def.getTriggeredUEI().orElse(null));
        dto.setRearmedUEI(def.getRearmedUEI().orElse(null));
        dto.setFilterOperator(def.getFilterOperator() == null ? null : def.getFilterOperator().getEnumName());
        dto.setResourceFilters(def.getResourceFilters().stream()
                .map(ThresholdingConfigMapper::toDto)
                .collect(Collectors.toList()));
    }

    public static ResourceFilterDto toDto(final ResourceFilter filter) {
        final ResourceFilterDto dto = new ResourceFilterDto();
        dto.setField(filter.getField());
        dto.setContent(filter.getContent().orElse(null));
        return dto;
    }

    // --------------------------------------------------------------- to entity

    public static Group toEntity(final ThresholdGroupDto dto) {
        final Group group = new Group();
        group.setName(trimToNull(dto.getName()));
        group.setRrdRepository(trimToNull(dto.getRrdRepository()));

        final List<Threshold> thresholds = new ArrayList<>();
        if (dto.getThresholds() != null) {
            for (final ThresholdDto thresholdDto : dto.getThresholds()) {
                thresholds.add(toEntity(thresholdDto));
            }
        }
        group.setThresholds(thresholds);

        final List<Expression> expressions = new ArrayList<>();
        if (dto.getExpressions() != null) {
            for (final ExpressionDto expressionDto : dto.getExpressions()) {
                expressions.add(toEntity(expressionDto));
            }
        }
        group.setExpressions(expressions);

        return group;
    }

    public static Threshold toEntity(final ThresholdDto dto) {
        final Threshold threshold = new Threshold();
        copyToEntity(dto, threshold);
        threshold.setDsName(trimToNull(dto.getDsName()));
        return threshold;
    }

    public static Expression toEntity(final ExpressionDto dto) {
        final Expression expression = new Expression();
        copyToEntity(dto, expression);
        expression.setExpression(trimToNull(dto.getExpression()));
        return expression;
    }

    private static void copyToEntity(final BaseThresholdDefDto dto, final Basethresholddef def) {
        // Only write relaxed when it is actually set: the getter coerces null to false, so writing it back
        // unconditionally would turn every round-trip into a diff of explicit relaxed="false" attributes.
        def.setRelaxed(Boolean.TRUE.equals(dto.getRelaxed()) ? Boolean.TRUE : null);
        def.setDescription(emptyToNull(dto.getDescription()));

        final ThresholdType type = dto.getType() == null ? null : ThresholdType.forName(dto.getType());
        def.setType(type);
        def.setDsType(trimToNull(dto.getDsType()));
        def.setValue(trimToNull(dto.getValue()));
        def.setRearm(trimToNull(dto.getRearm()));
        def.setTrigger(trimToNull(dto.getTrigger()));
        def.setDsLabel(emptyToNull(dto.getDsLabel()));
        def.setExprLabel(emptyToNull(dto.getExprLabel()));
        def.setTriggeredUEI(emptyToNull(dto.getTriggeredUEI()));

        // relativeChange and absoluteChange thresholds never re-arm, so a rearmedUEI on them would be dead
        // configuration. rearmingAbsoluteChange does re-arm and keeps its UEI.
        if (type == ThresholdType.RELATIVE_CHANGE || type == ThresholdType.ABSOLUTE_CHANGE) {
            def.setRearmedUEI(null);
        } else {
            def.setRearmedUEI(emptyToNull(dto.getRearmedUEI()));
        }

        // Leaving this unset lets the model apply its own default of OR.
        def.setFilterOperator(toFilterOperator(dto.getFilterOperator()));

        final List<ResourceFilter> filters = new ArrayList<>();
        if (dto.getResourceFilters() != null) {
            for (final ResourceFilterDto filterDto : dto.getResourceFilters()) {
                filters.add(toEntity(filterDto));
            }
        }
        def.setResourceFilters(filters);
    }

    public static ResourceFilter toEntity(final ResourceFilterDto dto) {
        final ResourceFilter filter = new ResourceFilter();
        filter.setField(trimToNull(dto.getField()));
        filter.setContent(emptyToNull(dto.getContent()));
        return filter;
    }

    public static FilterOperator toFilterOperator(final String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        for (final FilterOperator operator : FilterOperator.values()) {
            if (operator.getEnumName().equalsIgnoreCase(value.trim())) {
                return operator;
            }
        }
        return null;
    }

    private static String trimToNull(final String value) {
        if (value == null) {
            return null;
        }
        final String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * The JSP editor stored an empty text field as an absent attribute rather than an empty string, and the
     * daemon distinguishes the two. Keep doing that.
     */
    private static String emptyToNull(final String value) {
        return trimToNull(value);
    }
}
