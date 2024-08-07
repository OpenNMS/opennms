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
package org.opennms.netmgt.flows.classification.internal.validation;

import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.opennms.core.network.IPAddress;
import org.opennms.netmgt.flows.classification.FilterService;
import org.opennms.netmgt.flows.classification.error.ErrorContext;
import org.opennms.netmgt.flows.classification.error.Errors;
import org.opennms.netmgt.flows.classification.exception.InvalidRuleException;
import org.opennms.netmgt.flows.classification.internal.value.IpValue;
import org.opennms.netmgt.flows.classification.internal.value.StringValue;
import org.opennms.netmgt.flows.classification.persistence.api.Protocols;
import org.opennms.netmgt.flows.classification.persistence.api.Rule;

import com.google.common.base.Strings;
import com.google.common.net.InetAddresses;

public class RuleValidator {

    private static final Pattern PORT_PATTERN = Pattern.compile("^\\d+((-|,)\\d+)*$");

    private final FilterService filterService;

    public RuleValidator(FilterService filterService) {
        this.filterService = Objects.requireNonNull(filterService);
    }

    public void validate(Rule rule) throws InvalidRuleException {

        // Ensure that rule is associated to a group
        if (rule.getGroup() == null) {
            throw new InvalidRuleException(ErrorContext.Entity, Errors.RULE_GROUP_IS_REQUIRED);
        }
        // Name is required
        validateName(rule.getName());

        // Ensure that at least one field is defined (no catch all rule)
        if (!rule.hasDefinition()) {
            throw new InvalidRuleException(ErrorContext.Entity, Errors.RULE_NO_DEFINITIONS);
        }
        // Ensure protocol is defined correctly
        if (rule.hasProtocolDefinition()) {
            validateProtocol(rule.getProtocol());
        }
        // Ensure dst port is defined correctly
        if (rule.hasDstPortDefinition()) {
            validatePort(ErrorContext.DstPort, rule.getDstPort());
        }
        // Ensure dst ip address is defined correctly
        if (rule.hasDstAddressDefinition()) {
            validateIpAddress(ErrorContext.DstAddress, rule.getDstAddress());
        }
        // Ensure src port is defined correctly
        if (rule.hasSrcPortDefinition()) {
            validatePort(ErrorContext.SrcPort, rule.getSrcPort());
        }
        // Ensure src ip address is defined correctly
        if (rule.hasSrcAddressDefinition()) {
            validateIpAddress(ErrorContext.SrcAddress, rule.getSrcAddress());
        }
        // Ensure filter is defined correctly
        if (rule.hasExportFilterDefinition()) {
            filterService.validate(rule.getExporterFilter());
        }
    }

    protected static void validateName(String name) {
        if (Strings.isNullOrEmpty(name)) {
            throw new InvalidRuleException(ErrorContext.Name, Errors.RULE_NAME_IS_REQUIRED);
        }
    }

    protected static void validateProtocol(String protocol) throws InvalidRuleException {
        if (Strings.isNullOrEmpty(protocol)){
            throw new InvalidRuleException(ErrorContext.Protocol, Errors.RULE_PROTOCOL_IS_REQUIRED);
        }
        // Verify protocol actually exists
        for (StringValue eachProtocol : new StringValue(protocol).splitBy(",")) {
            if (Protocols.getProtocol(eachProtocol.getValue()) == null) {
                throw new InvalidRuleException(ErrorContext.Protocol, Errors.RULE_PROTOCOL_DOES_NOT_EXIST, eachProtocol.getValue());
            }
        }
    }

    protected static void validatePort(String errorContext, String port) throws InvalidRuleException {
        final StringValue portValue = new StringValue(port);
        if (portValue.isNullOrEmpty()) {
            throw new InvalidRuleException(errorContext, Errors.RULE_PORT_IS_REQUIRED);
        }
        if (portValue.hasWildcard()) {
            throw new InvalidRuleException(errorContext, Errors.RULE_PORT_NO_WILDCARD);
        }

        // Verify input
        final Matcher matcher = PORT_PATTERN.matcher(port);
        if (!matcher.matches()) {
            throw new InvalidRuleException(errorContext, Errors.RULE_PORT_DEFINITION_NOT_VALID);
        }

        // Try parsing input
        final List<StringValue> portValues = portValue.splitBy(",");
        final List<StringValue> rangedPortValues = portValues.stream().filter(v -> v.isRanged()).collect(Collectors.toList());
        rangedPortValues.forEach(v -> portValues.remove(v));

        // Verify the ranges
        for (StringValue eachRange : rangedPortValues) {
            final List<StringValue> range = eachRange.splitBy("-");

            // Verify each value is a number value
            for (int i=0; i<Math.min(range.size(), 2); i++) {
                verifyPortValue(errorContext, range.get(i).getValue());
            }

            // Check bounds
            int lowerBound = Integer.parseInt(range.get(0).getValue());
            int higherBound = range.size() == 1 ? lowerBound : Integer.parseInt(range.get(1).getValue());
            if (lowerBound > higherBound) {
                throw new InvalidRuleException(errorContext, Errors.RULE_PORT_RANGE_BOUNDS_NOT_VALID);
            }
        }

        // Verify normal values
        for (StringValue eachPort : portValues) {
            verifyPortValue(errorContext, eachPort.getValue());
        }
    }

    protected static void validateIpAddress(String errorContext, String ipAddressValue) throws InvalidRuleException {
        if (Strings.isNullOrEmpty(ipAddressValue)) {
            throw new InvalidRuleException(errorContext, Errors.RULE_IP_ADDRESS_INVALID, ipAddressValue);
        }
        final StringValue inputValue = new StringValue(ipAddressValue);
        final List<StringValue> actualValues = inputValue.splitBy(",");
        for (StringValue eachValue : actualValues) {
            // In case it is ranged, verify the range
            if (eachValue.isRanged()) {
                final List<StringValue> rangedValues = eachValue.splitBy("-");
                // either a-, or a-b-c, etc.
                if (rangedValues.size() != 2) {
                    throw new InvalidRuleException(errorContext, Errors.RULE_IP_ADDRESS_RANGE_INVALID, eachValue.getValue());
                }
                // Ensure each range is an ip address
                for (StringValue rangedValue : rangedValues) {
                    if (rangedValue.contains("/")) {
                        throw new InvalidRuleException(errorContext, Errors.RULE_IP_ADDRESS_RANGE_CIDR_NOT_SUPPORTED);
                    }
                    verifyIpAddress(errorContext, rangedValue.getValue());
                }
                // Now verify the range itself
                final IPAddress begin = new IPAddress(rangedValues.get(0).getValue());
                final IPAddress end = new IPAddress(rangedValues.get(1).getValue());
                if (begin.isGreaterThan(end)) {
                    throw new InvalidRuleException(errorContext, Errors.RULE_IP_ADDRESS_RANGE_BEGIN_END_INVALID, begin, end);
                }
            } else {
                if (eachValue.contains("/")) {
                    verifyCidrExpression(errorContext, eachValue.getValue());
                } else {
                    verifyIpAddress(errorContext, eachValue.getValue());
                }
            }
        }
    }

    // Validate each port to be between 0 - 65536
    private static void verifyPortValue(String errorContext, String input) throws InvalidRuleException {
        int value = Integer.parseInt(input);
        if (value < Rule.MIN_PORT_VALUE || value > Rule.MAX_PORT_VALUE) {
            throw new InvalidRuleException(errorContext, Errors.RULE_PORT_VALUE_NOT_IN_RANGE, Rule.MIN_PORT_VALUE, Rule.MAX_PORT_VALUE);
        }
    }

    // Validate each input to be a valid ip address
    private static void verifyIpAddress(final String errorContext, final String input) throws InvalidRuleException {
        if (!InetAddresses.isInetAddress(input)) {
            throw new InvalidRuleException(errorContext, Errors.RULE_IP_ADDRESS_INVALID, input);
        }
    }

    // Verify input is an actual valid cidr expression
    private static void verifyCidrExpression(String errorContext, String input) {
        try {
            IpValue.parseCIDR(input);
        } catch (IllegalArgumentException ex) {
            throw new InvalidRuleException(errorContext, Errors.RULE_IP_ADDRESS_INVALID_CIDR_EXPRESSION, input, ex.getMessage());
        }
    }
}
