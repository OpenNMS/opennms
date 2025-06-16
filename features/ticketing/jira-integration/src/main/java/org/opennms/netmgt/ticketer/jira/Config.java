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
