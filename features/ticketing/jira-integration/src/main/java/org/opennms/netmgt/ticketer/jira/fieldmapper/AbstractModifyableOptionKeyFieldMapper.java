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
