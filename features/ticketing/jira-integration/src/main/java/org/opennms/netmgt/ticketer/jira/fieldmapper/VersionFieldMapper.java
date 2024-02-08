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
import java.util.function.Supplier;

import com.atlassian.jira.rest.client.api.domain.FieldSchema;

public class VersionFieldMapper extends AbstractModifyableOptionKeyFieldMapper {

    public VersionFieldMapper(Supplier<Map<String, String>> optionKeySupplier) {
        super(optionKeySupplier);
    }

    @Override
    public boolean matches(FieldSchema schema) {
        return ("fixVersions".equals(schema.getSystem()) && schema.getCustom() == null)
                || ("versions".equals(schema.getSystem()) && schema.getCustom() == null)
                || "version".equals(schema.getType())
                || "version".equals(schema.getItems());
    }

    @Override
    public Object mapToFieldValue(String fieldId, FieldSchema schema, String attributeValue) {
        if ("array".equals(schema.getType())) {
            return new ArrayWrapper().map(eachItem -> createComplexIssueInputField(fieldId, "name", eachItem), attributeValue);
        }
        return createComplexIssueInputField(fieldId, "name", attributeValue);
    }
}
