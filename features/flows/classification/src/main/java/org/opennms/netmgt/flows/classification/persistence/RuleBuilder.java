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

package org.opennms.netmgt.flows.classification.persistence;

import com.google.common.base.Strings;

public class RuleBuilder {

    private Rule rule = new Rule();

    public RuleBuilder withName(String name) {
        rule.setName(name);
        return this;
    }

    public RuleBuilder withIpAddress(String ipAddress) {
        rule.setIpAddress(ipAddress);
        return this;
    }

    public RuleBuilder withPort(String port) {
        rule.setPort(port);
        return this;
    }

    public RuleBuilder withPort(int port) {
        rule.setPort("" + port);
        return this;
    }

    public RuleBuilder withProtocol(String protocol) {
        rule.setProtocol(protocol);
        return this;
    }

    public RuleBuilder withProtocol(Protocol protocol) {
        rule.setProtocol(protocol.getKeyword().toLowerCase());
        return this;
    }

    public Rule build() {
        if (Strings.isNullOrEmpty(rule.getName())) {
            throw new IllegalStateException("Cannot build rule, because required field 'name' is null or empty");
        }
        return rule;
    }

}
