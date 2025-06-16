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

import static org.opennms.netmgt.flows.classification.internal.validation.ValidatorTestUtils.verify;

import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.flows.classification.FilterService;
import org.opennms.netmgt.flows.classification.error.ErrorContext;
import org.opennms.netmgt.flows.classification.error.Errors;
import org.opennms.netmgt.flows.classification.persistence.api.Group;
import org.opennms.netmgt.flows.classification.persistence.api.GroupBuilder;
import org.opennms.netmgt.flows.classification.persistence.api.Protocol;
import org.opennms.netmgt.flows.classification.persistence.api.Protocols;
import org.opennms.netmgt.flows.classification.persistence.api.Rule;
import org.opennms.netmgt.flows.classification.persistence.api.RuleBuilder;
import org.slf4j.LoggerFactory;

public class RuleValidatorTest {

    private Group group;

    @Before
    public void before() {
        group = new GroupBuilder().withName("myGroup").build();
    }

    @Test
    public void verifyNameIsRequired() {
        final Rule rule = new Rule();
        rule.setGroup(group);
        verify(() -> new RuleValidator(FilterService.NOOP).validate(rule), Errors.RULE_NAME_IS_REQUIRED);
    }

    @Test
    public void verifyDefinitionIsRequired() {
        final Rule rule = new Rule();
        rule.setGroup(group);
        rule.setName("test");
        verify(() -> new RuleValidator(FilterService.NOOP).validate(rule), Errors.RULE_NO_DEFINITIONS);
    }

    @Test
    public void verifySuccess() {
        final Rule rule = new RuleBuilder()
                .withGroup(group)
                .withName("dummy")
                .withProtocol("tcp")
                .withDstAddress("8.8.8.8")
                .withDstPort("80").build();
        verify(() -> new RuleValidator(FilterService.NOOP).validate(rule));
    }

    @Test
    public void verifyProtocol() {
        // Fail
        verify(() -> RuleValidator.validateProtocol(""), Errors.RULE_PROTOCOL_IS_REQUIRED);
        verify(() -> RuleValidator.validateProtocol(null), Errors.RULE_PROTOCOL_IS_REQUIRED);
        verify(() -> RuleValidator.validateProtocol("xxxx"), Errors.RULE_PROTOCOL_DOES_NOT_EXIST);

        // Succeed
        verify(() -> RuleValidator.validateProtocol("tcp"));
        verify(() -> RuleValidator.validateProtocol("tcp,udp"));
        verify(() -> RuleValidator.validateProtocol("TCP,uDp"));

        // Verify all existing protocols
        for (Protocol eachProtocol : Protocols.getProtocols()) {
            if (!"".equals(eachProtocol.getKeyword())) {
                LoggerFactory.getLogger(getClass()).info("Verifying protocol {}", eachProtocol.getKeyword());
                verify(() -> RuleValidator.validateProtocol(eachProtocol.getKeyword()));
            }
        }
    }

    @Test
    public void verifyIpAddress() {
        // Note: The errorContext can either be srcAddress or dstAddress.

        // Fail
        verify(() -> RuleValidator.validateIpAddress(ErrorContext.SrcAddress, "*"), Errors.RULE_IP_ADDRESS_INVALID);
        verify(() -> RuleValidator.validateIpAddress(ErrorContext.SrcAddress, ""), Errors.RULE_IP_ADDRESS_INVALID);
        verify(() -> RuleValidator.validateIpAddress(ErrorContext.SrcAddress, null), Errors.RULE_IP_ADDRESS_INVALID);
        verify(() -> RuleValidator.validateIpAddress(ErrorContext.SrcAddress, "8.8"), Errors.RULE_IP_ADDRESS_INVALID);
        verify(() -> RuleValidator.validateIpAddress(ErrorContext.SrcAddress, "8.8.8.*"), Errors.RULE_IP_ADDRESS_INVALID);

        // Succeed
        verify(() -> RuleValidator.validateIpAddress(ErrorContext.SrcAddress, "ff01::1"));
        verify(() -> RuleValidator.validateIpAddress(ErrorContext.SrcAddress, "127.0.0.1"));
        verify(() -> RuleValidator.validateIpAddress(ErrorContext.SrcAddress, "10.0.0.0-10.255.255.255"));
    }

    @Test
    public void verifyPort() {
        // Note: The errorContext can either be srcPort or dstPort.

        // Fail
        verify(() -> RuleValidator.validatePort(ErrorContext.SrcPort, "*"), Errors.RULE_PORT_NO_WILDCARD);
        verify (() -> RuleValidator.validatePort(ErrorContext.SrcPort, ""), Errors.RULE_PORT_IS_REQUIRED);
        verify (() -> RuleValidator.validatePort(ErrorContext.SrcPort, null), Errors.RULE_PORT_IS_REQUIRED);
        verify(() -> RuleValidator.validatePort(ErrorContext.SrcPort, "80-a"), Errors.RULE_PORT_DEFINITION_NOT_VALID);
        verify(() -> RuleValidator.validatePort(ErrorContext.SrcPort, "80,a"), Errors.RULE_PORT_DEFINITION_NOT_VALID);
        verify(() -> RuleValidator.validatePort(ErrorContext.SrcPort, "100-80"), Errors.RULE_PORT_RANGE_BOUNDS_NOT_VALID);
        verify(() -> RuleValidator.validatePort(ErrorContext.SrcPort, "0-70000"), Errors.RULE_PORT_VALUE_NOT_IN_RANGE);
        verify(() -> RuleValidator.validatePort(ErrorContext.SrcPort, "65536"), Errors.RULE_PORT_VALUE_NOT_IN_RANGE);
        verify(() -> RuleValidator.validatePort(ErrorContext.SrcPort, "0-65536"), Errors.RULE_PORT_VALUE_NOT_IN_RANGE);

        // Succeed
        verify(() -> RuleValidator.validatePort(ErrorContext.SrcPort, "80"));
        verify(() -> RuleValidator.validatePort(ErrorContext.SrcPort, "80,8080"));
        verify(() -> RuleValidator.validatePort(ErrorContext.SrcPort, "80-8080"));
        verify(() -> RuleValidator.validatePort(ErrorContext.SrcPort, String.format("%d-%d", Rule.MIN_PORT_VALUE, Rule.MAX_PORT_VALUE)));
    }

    @Test
    public void verifyName() {
        // Fail
        verify(() -> RuleValidator.validateName(""), Errors.RULE_NAME_IS_REQUIRED);
        verify(() -> RuleValidator.validateName(null), Errors.RULE_NAME_IS_REQUIRED);

        // Succeed
        verify(() -> RuleValidator.validateName("test"));
        verify(() -> RuleValidator.validateName("Some-dummy String $%!"));
    }


}
