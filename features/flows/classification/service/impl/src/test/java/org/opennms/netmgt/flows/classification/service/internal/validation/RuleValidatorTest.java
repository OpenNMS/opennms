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

package org.opennms.netmgt.flows.classification.service.internal.validation;

import static org.opennms.netmgt.flows.classification.service.internal.validation.ValidatorTestUtils.verify;

import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.dao.mock.MockFilterDao;
import org.opennms.netmgt.flows.classification.service.error.ErrorContext;
import org.opennms.netmgt.flows.classification.service.error.Errors;
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
        ValidatorTestUtils.verify(() -> new RuleValidator(new MockFilterDao()).validate(rule), Errors.RULE_NAME_IS_REQUIRED);
    }

    @Test
    public void verifyDefinitionIsRequired() {
        final Rule rule = new Rule();
        rule.setGroup(group);
        rule.setName("test");
        ValidatorTestUtils.verify(() -> new RuleValidator(new MockFilterDao()).validate(rule), Errors.RULE_NO_DEFINITIONS);
    }

    @Test
    public void verifySuccess() {
        final Rule rule = new RuleBuilder()
                .withGroup(group)
                .withName("dummy")
                .withProtocol("tcp")
                .withDstAddress("8.8.8.8")
                .withDstPort("80").build();
        ValidatorTestUtils.verify(() -> new RuleValidator(new MockFilterDao()).validate(rule));
    }

    @Test
    public void verifyProtocol() {
        // Fail
        ValidatorTestUtils.verify(() -> RuleValidator.validateProtocol(""), Errors.RULE_PROTOCOL_IS_REQUIRED);
        ValidatorTestUtils.verify(() -> RuleValidator.validateProtocol(null), Errors.RULE_PROTOCOL_IS_REQUIRED);
        ValidatorTestUtils.verify(() -> RuleValidator.validateProtocol("xxxx"), Errors.RULE_PROTOCOL_DOES_NOT_EXIST);

        // Succeed
        ValidatorTestUtils.verify(() -> RuleValidator.validateProtocol("tcp"));
        ValidatorTestUtils.verify(() -> RuleValidator.validateProtocol("tcp,udp"));
        ValidatorTestUtils.verify(() -> RuleValidator.validateProtocol("TCP,uDp"));

        // Verify all existing protocols
        for (Protocol eachProtocol : Protocols.getProtocols()) {
            if (!"".equals(eachProtocol.getKeyword())) {
                LoggerFactory.getLogger(getClass()).info("Verifying protocol {}", eachProtocol.getKeyword());
                ValidatorTestUtils.verify(() -> RuleValidator.validateProtocol(eachProtocol.getKeyword()));
            }
        }
    }

    @Test
    public void verifyIpAddress() {
        // Note: The errorContext can either be srcAddress or dstAddress.

        // Fail
        ValidatorTestUtils.verify(() -> RuleValidator.validateIpAddress(ErrorContext.SrcAddress, "*"), Errors.RULE_IP_ADDRESS_INVALID);
        ValidatorTestUtils.verify(() -> RuleValidator.validateIpAddress(ErrorContext.SrcAddress, ""), Errors.RULE_IP_ADDRESS_INVALID);
        ValidatorTestUtils.verify(() -> RuleValidator.validateIpAddress(ErrorContext.SrcAddress, null), Errors.RULE_IP_ADDRESS_INVALID);
        ValidatorTestUtils.verify(() -> RuleValidator.validateIpAddress(ErrorContext.SrcAddress, "8.8"), Errors.RULE_IP_ADDRESS_INVALID);
        ValidatorTestUtils.verify(() -> RuleValidator.validateIpAddress(ErrorContext.SrcAddress, "8.8.8.*"), Errors.RULE_IP_ADDRESS_INVALID);

        // Succeed
        ValidatorTestUtils.verify(() -> RuleValidator.validateIpAddress(ErrorContext.SrcAddress, "ff01::1"));
        ValidatorTestUtils.verify(() -> RuleValidator.validateIpAddress(ErrorContext.SrcAddress, "127.0.0.1"));
        ValidatorTestUtils.verify(() -> RuleValidator.validateIpAddress(ErrorContext.SrcAddress, "10.0.0.0-10.255.255.255"));
    }

    @Test
    public void verifyPort() {
        // Note: The errorContext can either be srcPort or dstPort.

        // Fail
        ValidatorTestUtils.verify(() -> RuleValidator.validatePort(ErrorContext.SrcPort, "*"), Errors.RULE_PORT_NO_WILDCARD);
        ValidatorTestUtils.verify(() -> RuleValidator.validatePort(ErrorContext.SrcPort, ""), Errors.RULE_PORT_IS_REQUIRED);
        ValidatorTestUtils.verify(() -> RuleValidator.validatePort(ErrorContext.SrcPort, null), Errors.RULE_PORT_IS_REQUIRED);
        ValidatorTestUtils.verify(() -> RuleValidator.validatePort(ErrorContext.SrcPort, "80-a"), Errors.RULE_PORT_DEFINITION_NOT_VALID);
        ValidatorTestUtils.verify(() -> RuleValidator.validatePort(ErrorContext.SrcPort, "80,a"), Errors.RULE_PORT_DEFINITION_NOT_VALID);
        ValidatorTestUtils.verify(() -> RuleValidator.validatePort(ErrorContext.SrcPort, "100-80"), Errors.RULE_PORT_RANGE_BOUNDS_NOT_VALID);
        ValidatorTestUtils.verify(() -> RuleValidator.validatePort(ErrorContext.SrcPort, "0-70000"), Errors.RULE_PORT_VALUE_NOT_IN_RANGE);
        ValidatorTestUtils.verify(() -> RuleValidator.validatePort(ErrorContext.SrcPort, "65536"), Errors.RULE_PORT_VALUE_NOT_IN_RANGE);
        ValidatorTestUtils.verify(() -> RuleValidator.validatePort(ErrorContext.SrcPort, "0-65536"), Errors.RULE_PORT_VALUE_NOT_IN_RANGE);

        // Succeed
        ValidatorTestUtils.verify(() -> RuleValidator.validatePort(ErrorContext.SrcPort, "80"));
        ValidatorTestUtils.verify(() -> RuleValidator.validatePort(ErrorContext.SrcPort, "80,8080"));
        ValidatorTestUtils.verify(() -> RuleValidator.validatePort(ErrorContext.SrcPort, "80-8080"));
        ValidatorTestUtils.verify(() -> RuleValidator.validatePort(ErrorContext.SrcPort, String.format("%d-%d", Rule.MIN_PORT_VALUE, Rule.MAX_PORT_VALUE)));
    }

    @Test
    public void verifyName() {
        // Fail
        ValidatorTestUtils.verify(() -> RuleValidator.validateName(""), Errors.RULE_NAME_IS_REQUIRED);
        ValidatorTestUtils.verify(() -> RuleValidator.validateName(null), Errors.RULE_NAME_IS_REQUIRED);

        // Succeed
        ValidatorTestUtils.verify(() -> RuleValidator.validateName("test"));
        ValidatorTestUtils.verify(() -> RuleValidator.validateName("Some-dummy String $%!"));
    }


}
