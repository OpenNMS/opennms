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
