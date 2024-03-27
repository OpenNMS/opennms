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
package org.opennms.netmgt.collection.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.opennms.netmgt.collection.api.AttributeType;

public class AttributeTypeAdapter extends XmlAdapter<String,AttributeType> {

    private final String supportedTypeNames;

    public AttributeTypeAdapter() {
        // Build a string with the list of supported types
        // that can be used in exception messages
        final StringBuilder sb = new StringBuilder();
        sb.append("Supported types include: ");
        for (AttributeType knownType : AttributeType.values()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(knownType.getName());
        }
        sb.append(".");
        supportedTypeNames = sb.toString();
    }

    @Override
    public AttributeType unmarshal(String typeAsString) throws Exception {
        if (typeAsString == null) {
            throw new IllegalArgumentException("Type cannot be null.");
        }
        final AttributeType type = AttributeType.parse(typeAsString);
        if (type == null) {
            throw new IllegalArgumentException(String.format("Unsupported attribute type '%s'. %s",
                    typeAsString, supportedTypeNames));
        }
        return type;
    }

    @Override
    public String marshal(AttributeType type) throws Exception {
        if (type == null) {
            throw new IllegalArgumentException("Type cannot be null.");
        }
        return type.getName();
    }
}
