/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.ticketer.jira;

import java.util.Map;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;

import com.atlassian.jira.rest.client.api.domain.input.ComplexIssueInputFieldValue;
import com.google.common.collect.Maps;

public class FieldMapperRegistryTest {

    @Test
    public void verifyBuildLookupMap() {
        Properties props = new Properties();
        Assert.assertEquals(Boolean.TRUE, FieldMapperRegistry.buildLookupMap(props).isEmpty());

        props.put("jira.attributes.group.resolution", "name");
        props.put("jira.attributes.user.resolution", "custom");
        Map<String, String> keyLookupMap = FieldMapperRegistry.buildLookupMap(props);
        Assert.assertEquals(2, keyLookupMap.size());
        Assert.assertEquals("name", keyLookupMap.get("group"));
        Assert.assertEquals("custom", keyLookupMap.get("user"));
    }

    @Test
    public void verifyCreateComplexInputFieldValue() {
        // Verify null
        Assert.assertEquals(ComplexIssueInputFieldValue.with("name", "ulf"), FieldMapperRegistry.createComplexInputFieldValue(Maps.newHashMap(), "user", null, "ulf"));

        // Verify lookup
        Properties props = new Properties();
        props.put("jira.attributes.group.resolution", "name");
        props.put("jira.attributes.user.resolution", "custom");
        Map<String, String> lookupMap = FieldMapperRegistry.buildLookupMap(props);
        Assert.assertEquals(ComplexIssueInputFieldValue.with("custom", "ulf"), FieldMapperRegistry.createComplexInputFieldValue(lookupMap, "user", "name", "ulf"));
        Assert.assertEquals(ComplexIssueInputFieldValue.with("name", "jira-users"), FieldMapperRegistry.createComplexInputFieldValue(lookupMap, "group", "name", "jira-users"));
        Assert.assertEquals(ComplexIssueInputFieldValue.with("value", "core"), FieldMapperRegistry.createComplexInputFieldValue(lookupMap, "component", "value", "core"));
    }

}