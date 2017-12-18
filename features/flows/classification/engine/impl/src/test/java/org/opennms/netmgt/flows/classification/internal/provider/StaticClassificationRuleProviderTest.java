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

package org.opennms.netmgt.flows.classification.internal.provider;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.List;

import org.junit.Test;
import org.opennms.netmgt.flows.classification.persistence.api.Rule;

public class StaticClassificationRuleProviderTest {

    @Test
    public void verifyLoading() throws IOException {
        final StaticClassificationRuleProvider staticClassificationRuleProvider = new StaticClassificationRuleProvider();
        final List<Rule> rules = staticClassificationRuleProvider.getRules();
        assertThat(rules, not(empty()));

        Rule rule = staticClassificationRuleProvider.getRule("http", 80);
        assertNotNull(rule);
        assertThat(rule.getName(), equalToIgnoringCase("http"));
        assertThat(rule.getPort(), is("80"));
        assertThat(rule.getProtocol(), is("tcp,udp,sctp"));
    }

}