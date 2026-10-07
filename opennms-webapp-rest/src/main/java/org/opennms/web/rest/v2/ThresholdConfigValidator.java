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
package org.opennms.web.rest.v2;

import java.util.List;
import java.util.regex.Pattern;

import org.opennms.netmgt.config.threshd.ServiceStatus;
import org.opennms.netmgt.config.threshd.ThresholdType;
import org.opennms.web.rest.v2.mapper.ThresholdingConfigMapper;
import org.opennms.web.rest.v2.model.BaseThresholdDefDto;
import org.opennms.web.rest.v2.model.ExpressionDto;
import org.opennms.web.rest.v2.model.ParameterDto;
import org.opennms.web.rest.v2.model.ResourceFilterDto;
import org.opennms.web.rest.v2.model.ThreshdConfigDto;
import org.opennms.web.rest.v2.model.ThreshdPackageDto;
import org.opennms.web.rest.v2.model.ThreshdServiceDto;
import org.opennms.web.rest.v2.model.ThresholdDto;
import org.opennms.web.rest.v2.model.ThresholdGroupDto;

/**
 * Field-level validation for the threshold configuration REST services.
 *
 * <p>This has to run <em>before</em> the DTOs are mapped onto the config model: the model's setters assert
 * their arguments and throw {@link IllegalArgumentException}, which would surface as a 500 rather than the
 * 400 the client deserves.</p>
 *
 * <p>Each method returns a human-readable message, or {@code null} when the input is valid.</p>
 */
public abstract class ThresholdConfigValidator {

    /**
     * RRDtool caps datasource names at 19 characters. The XSD does not encode this, so the check lives here
     * (and in the UI) rather than in schema validation.
     */
    public static final int MAX_DS_NAME_LENGTH = 19;

    /**
     * Mirrors the {@code value}/{@code rearm} pattern in thresholding.xsd: a number, or a metadata reference
     * such as {@code ${scv:key}}. Anchored explicitly — XSD patterns are implicitly anchored, Java's are not,
     * so an unanchored copy would happily accept "1abc".
     */
    private static final Pattern NUMERIC_OR_METADATA =
            Pattern.compile("^(?:[-+]?[0-9]*\\.?[0-9]+(?:[eE][-+]?[0-9]+)?|\\$\\{(?:.+:.+)\\})$");

    /** Mirrors the {@code trigger} pattern in thresholding.xsd: a positive integer, or a metadata reference. */
    private static final Pattern POSITIVE_INTEGER_OR_METADATA =
            Pattern.compile("^(?:[0-9]*[1-9][0-9]*|\\$\\{(?:.+:.+)\\})$");

    private ThresholdConfigValidator() {
    }

    // ---------------------------------------------------------- thresholds.xml

    public static String validateGroup(final ThresholdGroupDto group) {
        if (group == null) {
            return "Missing threshold group in request body.";
        }
        if (isBlank(group.getName())) {
            return "Threshold group name is required.";
        }
        if (isBlank(group.getRrdRepository())) {
            return "Threshold group rrdRepository is required.";
        }

        if (group.getThresholds() != null) {
            for (int i = 0; i < group.getThresholds().size(); i++) {
                final String message = validateThreshold(group.getThresholds().get(i));
                if (message != null) {
                    return "Invalid threshold at index " + i + ": " + message;
                }
            }
        }
        if (group.getExpressions() != null) {
            for (int i = 0; i < group.getExpressions().size(); i++) {
                final String message = validateExpression(group.getExpressions().get(i));
                if (message != null) {
                    return "Invalid expression at index " + i + ": " + message;
                }
            }
        }
        return null;
    }

    public static String validateThreshold(final ThresholdDto threshold) {
        if (threshold == null) {
            return "threshold must not be null.";
        }
        if (isBlank(threshold.getDsName())) {
            return "ds-name is required.";
        }
        if (threshold.getDsName().trim().length() > MAX_DS_NAME_LENGTH) {
            return "ds-name '" + threshold.getDsName().trim() + "' is longer than " + MAX_DS_NAME_LENGTH
                    + " characters; RRDtool cannot store a datasource with a longer name.";
        }
        return validateCommon(threshold);
    }

    public static String validateExpression(final ExpressionDto expression) {
        if (expression == null) {
            return "expression must not be null.";
        }
        if (isBlank(expression.getExpression())) {
            return "expression is required.";
        }
        return validateCommon(expression);
    }

    private static String validateCommon(final BaseThresholdDefDto def) {
        if (isBlank(def.getType())) {
            return "type is required.";
        }
        if (ThresholdType.forName(def.getType().trim()) == null) {
            return "type '" + def.getType() + "' is not one of high, low, relativeChange, absoluteChange, "
                    + "rearmingAbsoluteChange.";
        }
        if (isBlank(def.getDsType())) {
            return "ds-type is required.";
        }
        if (isBlank(def.getValue())) {
            return "value is required.";
        }
        if (!NUMERIC_OR_METADATA.matcher(def.getValue().trim()).matches()) {
            return "value '" + def.getValue() + "' must be a number or a metadata reference such as ${scv:key}.";
        }
        // rearm and trigger stay required even for relativeChange and absoluteChange, where the daemon
        // ignores them: the XSD declares them mandatory, so omitting them writes an invalid document.
        if (isBlank(def.getRearm())) {
            return "rearm is required.";
        }
        if (!NUMERIC_OR_METADATA.matcher(def.getRearm().trim()).matches()) {
            return "rearm '" + def.getRearm() + "' must be a number or a metadata reference such as ${scv:key}.";
        }
        if (isBlank(def.getTrigger())) {
            return "trigger is required.";
        }
        if (!POSITIVE_INTEGER_OR_METADATA.matcher(def.getTrigger().trim()).matches()) {
            return "trigger '" + def.getTrigger() + "' must be a positive integer or a metadata reference.";
        }
        if (!isBlank(def.getFilterOperator())
                && ThresholdingConfigMapper.toFilterOperator(def.getFilterOperator()) == null) {
            return "filterOperator '" + def.getFilterOperator() + "' is not one of and, or.";
        }
        if (def.getResourceFilters() != null) {
            for (int i = 0; i < def.getResourceFilters().size(); i++) {
                final ResourceFilterDto filter = def.getResourceFilters().get(i);
                if (filter == null || isBlank(filter.getField())) {
                    return "resource filter at index " + i + " is missing its field name.";
                }
            }
        }
        return null;
    }

    // ------------------------------------------------ threshd-configuration.xml

    public static String validateThreshdConfig(final ThreshdConfigDto config) {
        if (config == null) {
            return "Missing threshd configuration in request body.";
        }
        if (config.getPackages() == null || config.getPackages().isEmpty()) {
            return "At least one threshd package is required.";
        }
        for (int i = 0; i < config.getPackages().size(); i++) {
            final String message = validatePackage(config.getPackages().get(i));
            if (message != null) {
                return "Invalid package at index " + i + ": " + message;
            }
        }
        return null;
    }

    public static String validatePackage(final ThreshdPackageDto pkg) {
        if (pkg == null) {
            return "Missing threshd package in request body.";
        }
        if (isBlank(pkg.getName())) {
            return "package name is required.";
        }
        if (isBlank(pkg.getFilter())) {
            return "package filter is required.";
        }
        if (pkg.getServices() != null) {
            for (int i = 0; i < pkg.getServices().size(); i++) {
                final String message = validateService(pkg.getServices().get(i));
                if (message != null) {
                    return "invalid service at index " + i + ": " + message;
                }
            }
        }
        return null;
    }

    public static String validateService(final ThreshdServiceDto service) {
        if (service == null) {
            return "service must not be null.";
        }
        if (isBlank(service.getName())) {
            return "service name is required.";
        }
        if (service.getInterval() == null || service.getInterval() < 1) {
            return "service interval is required and must be greater than 0.";
        }
        if (!isBlank(service.getStatus()) && toStatus(service.getStatus()) == null) {
            return "service status '" + service.getStatus() + "' is not one of on, off.";
        }
        return validateParameters(service.getParameters());
    }

    private static String validateParameters(final List<ParameterDto> parameters) {
        if (parameters == null) {
            return null;
        }
        for (int i = 0; i < parameters.size(); i++) {
            final ParameterDto parameter = parameters.get(i);
            if (parameter == null || isBlank(parameter.getKey())) {
                return "parameter at index " + i + " is missing its key.";
            }
            if (parameter.getValue() == null) {
                return "parameter '" + parameter.getKey() + "' is missing its value.";
            }
        }
        return null;
    }

    private static ServiceStatus toStatus(final String value) {
        for (final ServiceStatus status : ServiceStatus.values()) {
            if (status.name().equalsIgnoreCase(value.trim())) {
                return status;
            }
        }
        return null;
    }

    private static boolean isBlank(final String value) {
        return value == null || value.trim().isEmpty();
    }
}
