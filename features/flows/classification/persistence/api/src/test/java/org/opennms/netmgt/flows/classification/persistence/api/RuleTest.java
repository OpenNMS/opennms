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

package org.opennms.netmgt.flows.classification.persistence.api;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class RuleTest {

    @Test
    public void verifyMustHaveName() {
        Rule rule = new Rule();
        assertThat(rule.isValid(), is(false));

        rule.setPort("80");
        assertThat(rule.isValid(), is(false));

        rule.setIpAddress("10.10.10.10");
        assertThat(rule.isValid(), is(false));

        rule.setProtocol("tcp");
        assertThat(rule.isValid(), is(false));

        rule.setId(5);
        assertThat(rule.isValid(), is(false));

        rule.setName("http");
        assertThat(rule.isValid(), is(true));
    }

    @Test
    public void verifyNameOnlyIsNotEnough() {
        Rule rule = new Rule();
        rule.setName("http");

        assertThat(rule.isValid(), is(false));
        rule.setProtocol("tcp");
        assertThat(rule.isValid(), is(true));
    }
}