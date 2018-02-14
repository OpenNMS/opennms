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

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import com.atlassian.jira.rest.client.api.domain.FieldSchema;
import com.atlassian.jira.rest.client.api.domain.input.ComplexIssueInputFieldValue;
import com.google.common.base.Strings;

public class CascadingSelectFieldMapper extends AbstractModifyableOptionKeyFieldMapper {

    public CascadingSelectFieldMapper(Supplier<Map<String, String>> optionKeySupplier) {
        super(optionKeySupplier);
    }

    @Override
    public boolean matches(FieldSchema schema) {
        return "com.atlassian.jira.plugin.system.customfieldtypes:cascadingselect".equals(schema.getCustom());
    }

    @Override
    public Object mapToFieldValue(String fieldId, FieldSchema schema, String attributeValue) {
        if (!Strings.isNullOrEmpty(attributeValue)) {
            final String[] split = attributeValue.split(","); // split by ","
            if (split != null && split.length >= 1) { // we have at least one value
                final String optionKey = getOptionKey(fieldId, "value");
                final Map<String, Object> parentValueMap = new HashMap<>();
                parentValueMap.put(optionKey, split[0]); // set first value
                final ComplexIssueInputFieldValue parentValue = new ComplexIssueInputFieldValue(parentValueMap);
                if (split.length >= 2) { // if we have a 2nd value, set it as child of value 1
                    parentValue.getValuesMap().put("child", ComplexIssueInputFieldValue.with(optionKey, split[1]));
                }
                return parentValue;
            }
        }
        return null;
    }
}
