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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.opennms.web.rest.v2.model.ExpressionDto;
import org.opennms.web.rest.v2.model.IpRangeDto;
import org.opennms.web.rest.v2.model.ParameterDto;
import org.opennms.web.rest.v2.model.ResourceFilterDto;
import org.opennms.web.rest.v2.model.ThreshdConfigDto;
import org.opennms.web.rest.v2.model.ThreshdPackageDto;
import org.opennms.web.rest.v2.model.ThreshdServiceDto;
import org.opennms.web.rest.v2.model.ThresholdDto;
import org.opennms.web.rest.v2.model.ThresholdGroupDto;
import org.opennms.web.rest.v2.model.ThresholderDto;

public class ThresholdConfigValidatorTest {

    @Test
    public void acceptsNumbersAndMetadataReferencesForValueAndRearm() {
        for (final String value : List.of("0", "90", "-3.5", "+.5", "1e3", "1E-3", "${scv:key:value}")) {
            final ThresholdDto threshold = threshold();
            threshold.setValue(value);
            threshold.setRearm(value);
            assertNull("should accept " + value, ThresholdConfigValidator.validateThreshold(threshold));
        }
    }

    @Test
    public void rejectsNonNumericValues() {
        for (final String value : List.of("abc", "1,5", "1.2.3", "${nocolon}", "")) {
            final ThresholdDto threshold = threshold();
            threshold.setValue(value);
            assertNotNull("should reject " + value, ThresholdConfigValidator.validateThreshold(threshold));
        }
    }

    @Test
    public void anchorsThePatternsSoATrailingSuffixIsNotIgnored() {
        // XSD patterns are implicitly anchored; java.util.regex is not. An unanchored copy of the pattern
        // accepts "1abc" through Matcher.find(), which would then fail schema validation much later.
        final ThresholdDto threshold = threshold();
        threshold.setTrigger("1abc");
        assertNotNull(ThresholdConfigValidator.validateThreshold(threshold));

        threshold.setTrigger("3");
        threshold.setValue("90abc");
        assertNotNull(ThresholdConfigValidator.validateThreshold(threshold));
    }

    @Test
    public void requiresAPositiveIntegerTrigger() {
        for (final String trigger : List.of("1", "10", "${scv:a:b}")) {
            final ThresholdDto threshold = threshold();
            threshold.setTrigger(trigger);
            assertNull("should accept " + trigger, ThresholdConfigValidator.validateThreshold(threshold));
        }
        for (final String trigger : List.of("0", "-1", "1.5", "")) {
            final ThresholdDto threshold = threshold();
            threshold.setTrigger(trigger);
            assertNotNull("should reject " + trigger, ThresholdConfigValidator.validateThreshold(threshold));
        }
    }

    @Test
    public void requiresRearmAndTriggerEvenForTypesThatIgnoreThem() {
        // The XSD declares them mandatory regardless of the threshold type, so accepting a document without
        // them here would only move the failure to schema validation.
        final ThresholdDto threshold = threshold();
        threshold.setType("relativeChange");
        threshold.setRearm(null);
        assertNotNull(ThresholdConfigValidator.validateThreshold(threshold));

        threshold.setRearm("0");
        threshold.setTrigger(null);
        assertNotNull(ThresholdConfigValidator.validateThreshold(threshold));
    }

    @Test
    public void rejectsADsNameLongerThanRrdtoolSupports() {
        final ThresholdDto threshold = threshold();
        threshold.setDsName("a".repeat(ThresholdConfigValidator.MAX_DS_NAME_LENGTH));
        assertNull(ThresholdConfigValidator.validateThreshold(threshold));

        threshold.setDsName("a".repeat(ThresholdConfigValidator.MAX_DS_NAME_LENGTH + 1));
        final String message = ThresholdConfigValidator.validateThreshold(threshold);
        assertNotNull(message);
        assertTrue(message, message.contains("19"));
    }

    @Test
    public void requiresDsNameOnThresholdsAndExpressionOnExpressions() {
        final ThresholdDto threshold = threshold();
        threshold.setDsName(null);
        assertNotNull(ThresholdConfigValidator.validateThreshold(threshold));

        final ExpressionDto expression = expression();
        expression.setExpression("  ");
        assertNotNull(ThresholdConfigValidator.validateExpression(expression));

        // ...and neither field is required of the other kind.
        assertNull(ThresholdConfigValidator.validateExpression(expression()));
    }

    @Test
    public void rejectsAnUnknownThresholdType() {
        final ThresholdDto threshold = threshold();
        threshold.setType("sideways");
        assertNotNull(ThresholdConfigValidator.validateThreshold(threshold));

        threshold.setType("HIGH");
        assertNull("type matching is case insensitive", ThresholdConfigValidator.validateThreshold(threshold));
    }

    @Test
    public void rejectsAnUnknownFilterOperatorAndAFilterWithoutAField() {
        final ThresholdDto threshold = threshold();
        threshold.setFilterOperator("xor");
        assertNotNull(ThresholdConfigValidator.validateThreshold(threshold));

        threshold.setFilterOperator("AND");
        assertNull(ThresholdConfigValidator.validateThreshold(threshold));

        final ResourceFilterDto filter = new ResourceFilterDto();
        filter.setContent("^eth.*");
        threshold.setResourceFilters(List.of(filter));
        assertNotNull(ThresholdConfigValidator.validateThreshold(threshold));
    }

    @Test
    public void reportsWhichDefinitionInAGroupIsInvalid() {
        final ThresholdGroupDto group = group();
        final ThresholdDto broken = threshold();
        broken.setTrigger("0");
        group.setThresholds(List.of(threshold(), broken));

        final String message = ThresholdConfigValidator.validateGroup(group);

        assertNotNull(message);
        assertTrue(message, message.startsWith("Invalid threshold at index 1:"));
    }

    @Test
    public void requiresAGroupNameAndRrdRepository() {
        final ThresholdGroupDto group = group();
        group.setName("  ");
        assertNotNull(ThresholdConfigValidator.validateGroup(group));

        group.setName("mib2");
        group.setRrdRepository(null);
        assertNotNull(ThresholdConfigValidator.validateGroup(group));

        assertEquals("Missing threshold group in request body.", ThresholdConfigValidator.validateGroup(null));
    }

    @Test
    public void requiresAtLeastOnePackageAndAPositiveThreadCount() {
        final ThreshdConfigDto config = threshdConfig();
        config.setThreads(0);
        assertNotNull(ThresholdConfigValidator.validateThreshdConfig(config));

        config.setThreads(5);
        config.setPackages(List.of());
        assertNotNull(ThresholdConfigValidator.validateThreshdConfig(config));

        config.setPackages(List.of(threshdPackage()));
        assertNull(ThresholdConfigValidator.validateThreshdConfig(config));
    }

    @Test
    public void requiresAPackageNameAndFilter() {
        final ThreshdPackageDto pkg = threshdPackage();
        pkg.setFilter(null);
        assertNotNull(ThresholdConfigValidator.validatePackage(pkg));

        pkg.setFilter("IPADDR != '0.0.0.0'");
        pkg.setName("");
        assertNotNull(ThresholdConfigValidator.validatePackage(pkg));
    }

    @Test
    public void requiresAPositiveServiceIntervalAndAKnownStatus() {
        final ThreshdServiceDto service = service();
        service.setInterval(0L);
        assertNotNull(ThresholdConfigValidator.validateService(service));

        service.setInterval(300000L);
        service.setStatus("paused");
        assertNotNull(ThresholdConfigValidator.validateService(service));

        service.setStatus("OFF");
        assertNull(ThresholdConfigValidator.validateService(service));
    }

    @Test
    public void requiresBothHalvesOfAParameter() {
        final ThreshdServiceDto service = service();
        final ParameterDto parameter = new ParameterDto();
        parameter.setValue("mib2");
        service.setParameters(List.of(parameter));
        assertNotNull(ThresholdConfigValidator.validateService(service));

        parameter.setKey("thresholding-group");
        parameter.setValue(null);
        assertNotNull(ThresholdConfigValidator.validateService(service));
    }

    @Test
    public void requiresAThresholderServiceAndClassName() {
        final ThresholderDto thresholder = new ThresholderDto();
        assertNotNull(ThresholdConfigValidator.validateThresholder(thresholder));

        thresholder.setService("SNMP");
        assertNotNull(ThresholdConfigValidator.validateThresholder(thresholder));

        thresholder.setClassName("org.opennms.netmgt.threshd.SnmpThresholder");
        assertNull(ThresholdConfigValidator.validateThresholder(thresholder));
    }

    // ------------------------------------------------------------------ fixtures

    private static ThresholdDto threshold() {
        final ThresholdDto dto = new ThresholdDto();
        dto.setDsName("cpuUtilization");
        dto.setType("high");
        dto.setDsType("node");
        dto.setValue("90");
        dto.setRearm("70");
        dto.setTrigger("3");
        return dto;
    }

    private static ExpressionDto expression() {
        final ExpressionDto dto = new ExpressionDto();
        dto.setExpression("a + b");
        dto.setType("high");
        dto.setDsType("node");
        dto.setValue("90");
        dto.setRearm("70");
        dto.setTrigger("3");
        return dto;
    }

    private static ThresholdGroupDto group() {
        final ThresholdGroupDto dto = new ThresholdGroupDto();
        dto.setName("mib2");
        dto.setRrdRepository("/opt/opennms/share/rrd/snmp/");
        return dto;
    }

    private static ThreshdConfigDto threshdConfig() {
        final ThreshdConfigDto dto = new ThreshdConfigDto();
        dto.setThreads(5);
        dto.setPackages(List.of(threshdPackage()));
        return dto;
    }

    private static ThreshdPackageDto threshdPackage() {
        final ThreshdPackageDto dto = new ThreshdPackageDto();
        dto.setName("example1");
        dto.setFilter("IPADDR != '0.0.0.0'");
        final IpRangeDto range = new IpRangeDto();
        range.setBegin("1.1.1.1");
        range.setEnd("254.254.254.254");
        dto.setIncludeRanges(List.of(range));
        dto.setServices(List.of(service()));
        return dto;
    }

    private static ThreshdServiceDto service() {
        final ParameterDto parameter = new ParameterDto();
        parameter.setKey("thresholding-group");
        parameter.setValue("mib2");

        final ThreshdServiceDto dto = new ThreshdServiceDto();
        dto.setName("SNMP");
        dto.setInterval(300000L);
        dto.setStatus("on");
        dto.setParameters(List.of(parameter));
        return dto;
    }
}
