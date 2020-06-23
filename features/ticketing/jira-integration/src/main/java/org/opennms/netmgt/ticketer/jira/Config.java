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
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

/**
 * Configuration abstraction to make access to the underlying properties easier.
 *
 * @author mvrueden
 */
public class Config {

    private final Properties properties;

    public Config(Properties properties) {
        this.properties = Objects.requireNonNull(properties);
    }

    public String getHost() {
        return getTrimmedProperty("jira.host");
    }

    public String getUsername() {
        return getTrimmedProperty("jira.username");
    }

    public String getPassword() {
        return properties.getProperty("jira.password"); // we do not trim password, as SPACE maybe part of the password
    }

    public Long getIssueTypeId() throws NumberFormatException {
        String trimmedProperty = getTrimmedProperty("jira.type");
        try {
            return Long.valueOf(trimmedProperty);
        } catch (NumberFormatException nfe) {
            throw new NumberFormatException("The value '" + trimmedProperty + "' for property 'jira.type' is not a valid number.");
        }
    }

    public String getProjectKey() {
        return getTrimmedProperty("jira.project");
    }

    public String getResolveTransitionName() {
        return getTrimmedProperty("jira.resolve");
    }

    public String getReopentransitionName() {
        return getTrimmedProperty("jira.reopen");
    }

    public Long getCacheReloadTime() throws NumberFormatException {
        String value = getTrimmedProperty("jira.cache.reloadTime");
        if (!Strings.isNullOrEmpty(value) && !Strings.isNullOrEmpty(value.trim())) {
            return Long.valueOf(value.trim());
        }
        return null;
    }

    public List<String> getOpenStatus() {
        return buildList(getTrimmedProperty("jira.status.open"));
    }

    public List<String> getCloseStatus() {
        return buildList(getTrimmedProperty("jira.status.closed"));
    }

    public List<String> getCancelStatus() {
        return buildList(getTrimmedProperty("jira.status.cancelled"));
    }

    public Properties getProperties() {
        return properties;
    }

    private String getTrimmedProperty(String key) {
        String property = properties.getProperty(key);
        if (property != null) {
            return property.trim();
        }
        return null;
    }

    public void validateRequiredProperties() {
        List<String> optional = Lists.newArrayList("jira.username", "jira.password");
        for (String eachKey : properties.stringPropertyNames()) {
            if (!optional.contains(eachKey)) {
                if (Strings.isNullOrEmpty(properties.getProperty(eachKey))) {
                    throw new RuntimeException("Property '" + eachKey + "' is required, but was null or empty");
                }
            }
        }
    }

    private static List<String> buildList(String input) {
        if (!Strings.isNullOrEmpty(input)) {
            return Arrays.stream(input.split(",")).map(String::trim).filter(v -> !v.isEmpty()).collect(Collectors.toList());
        }
        return Lists.newArrayList();
    }
}
