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
import java.util.Objects;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;

import com.atlassian.jira.rest.client.api.domain.FieldSchema;
import com.atlassian.jira.rest.client.api.domain.input.ComplexIssueInputFieldValue;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

public class FieldMapperRegistryTest {

    // Helper to build a jira field schema
    private static class FieldSchemaBuilder {
        private String items;
        private String type;
        private String system;
        private Long customId;
        private String custom;

        public FieldSchemaBuilder withItems(String items) {
            this.items = items;
            return this;
        }

        public FieldSchemaBuilder withType(String type) {
            this.type = type;
            return this;
        }

        public FieldSchemaBuilder withSystem(String system) {
            this.system = system;
            return this;
        }

        public FieldSchemaBuilder withCustom(String custom) {
            Objects.requireNonNull(custom);
            this.customId = 1L;
            this.custom = custom;
            return this;
        }

        public FieldSchema build() {
            return new FieldSchema(type, items, system, custom, customId);
        }
    }

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
    public void verifyCustomKeyLookup() {
        // Verify null
        Assert.assertEquals(ComplexIssueInputFieldValue.with("name", "ulf"), getValue(new Properties(), null, new FieldSchemaBuilder().withType("user").build(), "ulf"));

        // Verify lookup
        Properties props = new Properties();
        props.put("jira.attributes.group.resolution", "id");
        props.put("jira.attributes.user.resolution", "custom");
        Assert.assertEquals(ComplexIssueInputFieldValue.with("custom", "ulf"), getValue(props, "user", new FieldSchemaBuilder().withType("user").build(), "ulf"));
        Assert.assertEquals(ComplexIssueInputFieldValue.with("id", "1234"), getValue(props, "group", new FieldSchemaBuilder().withType("group").build(), "1234"));
    }

    // System fields
    @Test
    public void verifySystemFieldMapping() {
        // Issue Type
        Assert.assertEquals(ComplexIssueInputFieldValue.with("name", "Bug"),
                getValue(new FieldSchemaBuilder()
                        .withType("issuetype")
                        .withSystem("issuetype")
                        .build(), "Bug"));

        // Description
        Assert.assertEquals("My description",
                getValue(new FieldSchemaBuilder()
                        .withType("string")
                        .withSystem("description")
                        .build(), "My description"));

        // Project
        Assert.assertEquals(ComplexIssueInputFieldValue.with("key", "NMS"),
                getValue(new FieldSchemaBuilder()
                        .withType("project")
                        .withSystem("project")
                        .build(), "NMS"));

        // Fix Versions
        Assert.assertEquals(Lists.newArrayList(ComplexIssueInputFieldValue.with("name", "20.0.0"), ComplexIssueInputFieldValue.with("name", "21.0.0")),
                getValue(new FieldSchemaBuilder()
                    .withItems("version")
                    .withType("array")
                    .withSystem("fixVersions")
                    .build(), "20.0.0,21.0.0"));

        // Affected Versions
        Assert.assertEquals(Lists.newArrayList(ComplexIssueInputFieldValue.with("name", "20.0.0"), ComplexIssueInputFieldValue.with("name", "21.0.0")),
                getValue(new FieldSchemaBuilder()
                        .withItems("version")
                        .withType("array")
                        .withSystem("versions")
                        .build(), "20.0.0,21.0.0"));

        // Summary
        Assert.assertEquals("Dummy summary",
                getValue(new FieldSchemaBuilder()
                    .withType("string")
                    .withSystem("summary")
                    .build(), "Dummy summary"));

        // Reporter
        Assert.assertEquals(ComplexIssueInputFieldValue.with("name", "ulf"),
                getValue(new FieldSchemaBuilder()
                    .withType("user")
                    .withSystem("reporter")
                    .build(), "ulf"));

        // Assignee
        Assert.assertEquals(ComplexIssueInputFieldValue.with("name", "ulf"),
                getValue(new FieldSchemaBuilder()
                        .withType("user")
                        .withSystem("assignee")
                        .build(), "ulf"));

        // Priority
        Assert.assertEquals(ComplexIssueInputFieldValue.with("name", "Trivial"),
                getValue(new FieldSchemaBuilder()
                        .withType("priority")
                        .withSystem("priority")
                        .build(), "Trivial"));

        // Environment
        Assert.assertEquals("Dummy environment",
                getValue(new FieldSchemaBuilder()
                        .withType("string")
                        .withSystem("environment")
                        .build(), "Dummy environment"));

        // Duedate
        Assert.assertEquals("2017-01-31",
                getValue(new FieldSchemaBuilder()
                        .withType("date")
                        .withSystem("duedate")
                        .build(), "2017-01-31"));

        // Labels
        Assert.assertEquals(Lists.newArrayList("label1", "label2", "label with space"),
                getValue(new FieldSchemaBuilder()
                        .withItems("string")
                        .withType("array")
                        .withSystem("labels")
                        .build(), "label1,label2,label with space"));

        // Component
        Assert.assertEquals(
                Lists.newArrayList(
                        ComplexIssueInputFieldValue.with("name", "component1"),
                        ComplexIssueInputFieldValue.with("name", "component with space")
                ),
                getValue(new FieldSchemaBuilder()
                        .withType("array")
                        .withItems("component")
                        .withSystem("components")
                        .build(), "component1,component with space"));
    }

    // Custom fields (one of each type)
    @Test
    public void verifyCustomFieldMapping() {
        // Date field
        Assert.assertEquals("2011-10-03",
                getValue(new FieldSchemaBuilder()
                        .withType("date")
                        .withCustom("com.atlassian.jira.plugin.system.customfieldtypes:datepicker")
                        .build(), "2011-10-03"));

        // Date time field
        Assert.assertEquals("2011-10-19T10:29:29.908+1100",
                getValue(new FieldSchemaBuilder()
                    .withType("datetime")
                        .withCustom("com.atlassian.jira.plugin.system.customfieldtypes:datetime")
                        .build(), "2011-10-19T10:29:29.908+1100"));
        // Number field
        Assert.assertEquals(3.1415,
                getValue(new FieldSchemaBuilder()
                    .withType("number")
                    .withCustom("com.atlassian.jira.plugin.system.customfieldtypes:float")
                    .build(), "3.1415"));

        // Text field (Single line)
        Assert.assertEquals("I am a single text line",
                getValue(new FieldSchemaBuilder()
                        .withType("string")
                        .withCustom("com.atlassian.jira.plugin.system.customfieldtypes:textfield")
                        .build(), "I am a single text line"));

        // Text field (Multi line)
        Assert.assertEquals("I am a \nmulti\nline\ntext",
                getValue(new FieldSchemaBuilder()
                        .withType("string")
                        .withCustom("com.atlassian.jira.plugin.system.customfieldtypes:textarea")
                        .build(), "I am a \nmulti\nline\ntext"));

        // URL
        Assert.assertEquals("http://www.opennms.org",
                getValue(new FieldSchemaBuilder()
                    .withType("string")
                    .withCustom("com.atlassian.jira.plugin.system.customfieldtypes:url")
                    .build(), "http://www.opennms.org"));

        // Labels
        Assert.assertEquals(Lists.newArrayList("label1", "label2", "label with space"),
                getValue(new FieldSchemaBuilder()
                    .withItems("string")
                    .withType("array")
                    .withCustom("com.atlassian.jira.plugin.system.customfieldtypes:labels")
                    .build(), "label1,label2,label with space"));

        // Project picker
        Assert.assertEquals(ComplexIssueInputFieldValue.with("key", "NMS"),
                getValue(new FieldSchemaBuilder()
                    .withType("project")
                    .withCustom("com.atlassian.jira.plugin.system.customfieldtypes:project")
                    .build(), "NMS"));

        // Radio button
        Assert.assertEquals(ComplexIssueInputFieldValue.with("value", "red"),
                getValue(new FieldSchemaBuilder()
                    .withType("string")
                    .withCustom("com.atlassian.jira.plugin.system.customfieldtypes:radiobuttons")
                    .build(), "red"));

        // Single select
        Assert.assertEquals(ComplexIssueInputFieldValue.with("value", "blue"),
                getValue(new FieldSchemaBuilder()
                        .withType("string")
                        .withCustom("com.atlassian.jira.plugin.system.customfieldtypes:select")
                        .build(), "blue"));

        // Multi select
        Assert.assertEquals(
                Lists.newArrayList(
                    ComplexIssueInputFieldValue.with("value", "red"),
                    ComplexIssueInputFieldValue.with("value", "blue"),
                    ComplexIssueInputFieldValue.with("value", "item with space")
                ),
                getValue(new FieldSchemaBuilder()
                        .withItems("string")
                        .withType("array")
                        .withCustom("com.atlassian.jira.plugin.system.customfieldtypes:multiselect")
                        .build(), "red,blue,item with space"));

        // Checkboxes
        Assert.assertEquals(
                Lists.newArrayList(
                    ComplexIssueInputFieldValue.with("value", "red"), ComplexIssueInputFieldValue.with("value", "item with space!")
                ),
                getValue(new FieldSchemaBuilder()
                        .withItems("string")
                        .withType("array")
                        .withCustom("com.atlassian.jira.plugin.system.customfieldtypes:multicheckboxes")
                        .build(), "red,item with space!"));

        // Cascading Select
        Assert.assertEquals(new ComplexIssueInputFieldValue(
                ImmutableMap.<String, Object>builder()
                    .put("value", "parent value")
                    .put("child", ComplexIssueInputFieldValue.with("value", "child value"))
                    .build()),
                getValue(new FieldSchemaBuilder()
                    .withType("array")
                    .withItems("string")
                    .withCustom("com.atlassian.jira.plugin.system.customfieldtypes:cascadingselect")
                    .build(), "parent value,child value"));

        // Group picker (single)
        Assert.assertEquals(ComplexIssueInputFieldValue.with("name", "jira-users"),
                getValue(new FieldSchemaBuilder()
                        .withType("group")
                        .withCustom("com.atlassian.jira.plugin.system.customfieldtypes:grouppicker")
                        .build(), "jira-users"));


        // Group picker (multi)
        Assert.assertEquals(
                Lists.newArrayList(
                    ComplexIssueInputFieldValue.with("name", "jira-users"),
                    ComplexIssueInputFieldValue.with("name", "jira-developers"),
                    ComplexIssueInputFieldValue.with("name", "jira-administrators")
                ),
                getValue(new FieldSchemaBuilder()
                        .withType("array")
                        .withItems("group")
                        .withCustom("com.atlassian.jira.plugin.system.customfieldtypes:multigrouppicker")
                        .build(), "jira-users,jira-developers,jira-administrators"));

        // Version picker (single)
        Assert.assertEquals(ComplexIssueInputFieldValue.with("name", "20.0.0"),
                getValue(new FieldSchemaBuilder()
                        .withType("version")
                        .withCustom("com.atlassian.jira.plugin.system.customfieldtypes:version")
                        .build(), "20.0.0"));


        // Version picker (multiple)
        Assert.assertEquals(
                Lists.newArrayList(
                        ComplexIssueInputFieldValue.with("name", "18.0.0"),
                        ComplexIssueInputFieldValue.with("name", "18.0.1"),
                        ComplexIssueInputFieldValue.with("name", "19.0.0")),
                getValue(new FieldSchemaBuilder()
                        .withType("array")
                        .withItems("version")
                        .withCustom("com.atlassian.jira.plugin.system.customfieldtypes:multiversion")
                        .build(), "18.0.0,18.0.1,19.0.0"));

        // User picker (single)
        Assert.assertEquals(ComplexIssueInputFieldValue.with("name", "ulf"),
                getValue(new FieldSchemaBuilder()
                        .withType("user")
                        .withCustom("com.atlassian.jira.plugin.system.customfieldtypes:userpicker")
                        .build(), "ulf"));


        // User picker (multiple)
        Assert.assertEquals(
                Lists.newArrayList(
                    ComplexIssueInputFieldValue.with("name", "mvr"),
                    ComplexIssueInputFieldValue.with("name", "ulf")
                ),
                getValue(new FieldSchemaBuilder()
                        .withType("array")
                        .withItems("user")
                        .withCustom("com.atlassian.jira.plugin.system.customfieldtypes:multiuserpicker")
                        .build(), "mvr,ulf"));

    }

    private static Object getValue(FieldSchema fieldSchema, String attributeValue) {
        return getValue(new Properties(), null, fieldSchema, attributeValue);
    }

    private static Object getValue(Properties jiraProperties, String fieldId, FieldSchema fieldSchema, String attributeValue) {
        return new FieldMapperRegistry(jiraProperties).lookup(fieldSchema).mapToFieldValue(fieldId, fieldSchema, attributeValue);
    }
}