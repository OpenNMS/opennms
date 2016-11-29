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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.atlassian.jira.rest.client.api.domain.input.ComplexIssueInputFieldValue;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import com.google.common.base.Strings;

/**
 * The {@link FieldMapperRegistry} maps String input values to a JIRA ReST API representation.
 *
 * @see IssueInputBuilder#setFieldValue(String, Object)
 *
 * @author mvrueden
 */
public class FieldMapperRegistry {

    private final Map<String, Function<String, ?>> functionMap = new HashMap<>();

    public FieldMapperRegistry(Properties properties) {
        Objects.requireNonNull(properties);
        Map<String, String> lookupMap = buildLookupMap(properties);

        // Each function maps a field by its type to the JIRA ReST API representation (ComplexInputFieldValue in most cases)
        functionMap.put("number", input -> Long.valueOf(input));
        functionMap.put("group", input -> createComplexInputFieldValue(lookupMap, "group", "name", input));
        functionMap.put("user", input -> createComplexInputFieldValue(lookupMap, "user", "name", input));
        functionMap.put("issuetype", input -> createComplexInputFieldValue(lookupMap, "issuetype", "name", input));
        functionMap.put("priority", input -> createComplexInputFieldValue(lookupMap, "priority", "name", input));
        functionMap.put("version", input -> createComplexInputFieldValue(lookupMap, "version", "name", input));
        functionMap.put("component", input -> createComplexInputFieldValue(lookupMap, "component", "name", input));
        functionMap.put("option", input -> createComplexInputFieldValue(lookupMap, "option", "value", input));
        functionMap.put("project", input -> createComplexInputFieldValue(lookupMap, "project", "key", input));

        // option-with-child values can be null, empty, "value" or "value1,value2".
        functionMap.put("option-with-child", input -> {
            if (!Strings.isNullOrEmpty(input)) {
                final String[] split = input.split(","); // split by ","
                if (split != null && split.length >= 1) { // we have at least one value
                    final Map<String, Object> parentValueMap = new HashMap<>();
                    parentValueMap.put("value", split[0]); // set first value
                    final ComplexIssueInputFieldValue parentValue = new ComplexIssueInputFieldValue(parentValueMap);
                    if (split.length >= 2) { // if we have a 2nd value, set it as child of value 1
                        parentValue.getValuesMap().put("child", ComplexIssueInputFieldValue.with("value", split[1]));
                    }
                    return parentValue;
                }
            }
            return null;
        });
    }

    protected static ComplexIssueInputFieldValue createComplexInputFieldValue(Map<String, String> lookupMap, String lookupKey, String defaultKey, String input) {
        // a concrete key is defined
        if (lookupMap.containsKey(lookupKey)) {
            return ComplexIssueInputFieldValue.with(lookupMap.get(lookupKey), input);
        }
        // no default key is defined, in most cases "name" is the right key.
        if (Strings.isNullOrEmpty(defaultKey)) {
            return ComplexIssueInputFieldValue.with("name", input);
        }
        // no concrete key is defined, but we have a fallback key, we use that
        return ComplexIssueInputFieldValue.with(defaultKey, input);
    }

    /**
     * The jira.properties may contain properties to define the lookup strategy for fields.
     * Usually the default is to lookup a (custom) field by it's name. This can be overwritten, e.g.
     * jira.attributes.project.resolution=id to overwrite it's default key lookup to id lookup.
     *
     * @param properties
     * @return
     */
    protected static Map<String, String> buildLookupMap(Properties properties) {
        final Map<String, String> lookupMap = new HashMap<>();
        for (String eachKey : properties.stringPropertyNames()) {
            if (eachKey.length() > "jira.attributes.resolution".length() && eachKey.startsWith("jira.attributes.") && eachKey.endsWith("resolution")) {
                lookupMap.put(eachKey.substring("jira.attributes.".length(), eachKey.length() - "resolution".length() - 1), properties.getProperty(eachKey));
            }
        }
        return lookupMap;
    }

    /**
     * Helper method to lookup a Function to convert a value by its type and variant to the Jira ReST API representation.
     *
     * @param type The type, e.g. "user", "group", "version", "array", etc.
     * @param variant The variant. Is only set if "type" is "array". In that case, the "variant" defines each element in the array (e.g. "user", "version", etc)
     * @return A function to transform the input to its Jira ReST API representation.
     */
    public Function<String, ?> lookup(String type, String variant) {
        // If we have an array and a type set, we split the values to build the array
        // and apply the "variant" function to each element in the array.
        // E.g. "label1,label2" becomes ["label1", "label2"] if variant is "string"
        // "label1,label" becomes [{"name":"label1"}, {"name":"label2"}]" if variant is for example "version"
        if ("array".equals(type) && !Strings.isNullOrEmpty(variant)) {
            return (Function<String, Object>) input -> {
                Function<String, ?> function = lookup(variant);
                return Arrays.stream(input.split(",")).map(v -> function.apply(v)).collect(Collectors.toList());
            };
        }
        return lookup(type); // no array (Note: variant should be empty or null in this case)
    }

    /**
     * Returns the function, which is registered with <code>key</code>, if no such function exists, {@link Function#identity()} is returned.
     *
     * @param key The key to lookup the function for
     * @return the function, which is registered with <code>key</code>, if no such function exists, {@link Function#identity()} is returned.
     */
    private Function<String, ?> lookup(String key) {
        final Function<String, ?> function = functionMap.get(key);
        if (function != null) {
            return function;
        }
        return Function.identity(); // Default
    }
}
