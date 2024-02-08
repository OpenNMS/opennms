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
package org.opennms.netmgt.ticketer.jira.fieldmapper;

import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.function.Supplier;

import com.atlassian.jira.rest.client.api.domain.input.ComplexIssueInputFieldValue;

/**
 * {@link FieldMapper} with a modifyable option key, such as component, project etc.
 *
 * @author mvrueden
 */
public abstract class AbstractModifyableOptionKeyFieldMapper implements FieldMapper {

    private final Supplier<Map<String, String>> optionKeySupplier;

    protected AbstractModifyableOptionKeyFieldMapper(Supplier<Map<String, String>> optionKeySupplier) {
        this.optionKeySupplier = Objects.requireNonNull(optionKeySupplier);
    }

    /**
     * Allows overwriting the optionKey with the value defined in {@link #optionKeySupplier}.
     *
     * @param fieldId the id of the field
     * @param defaultKey the default option key (e.g. value, name, key, etc.)
     * @param value The value to set
     * @return The {@link ComplexIssueInputFieldValue} either with defaultKey or the key defined in jira.properties.
     *
     * @see org.opennms.netmgt.ticketer.jira.FieldMapperRegistry#buildLookupMap(Properties)
     */
    public ComplexIssueInputFieldValue createComplexIssueInputField(String fieldId, String defaultKey, Object value) {
        Objects.requireNonNull(defaultKey);
        return ComplexIssueInputFieldValue.with(getOptionKey(fieldId, defaultKey), value);
    }

    public String getOptionKey(String fieldId, String defaultKey) {
        if (fieldId != null) {
            final Map<String, String> lookupMap = optionKeySupplier.get();
            if (optionKeySupplier != null && lookupMap.get(fieldId) != null && !lookupMap.isEmpty()) {
                return lookupMap.get(fieldId);
            }
        }
        return defaultKey;
    }
}
