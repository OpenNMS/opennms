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
package org.opennms.features.config.dao.impl.util;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class XmlSchema {

    private final String xsdContent;
    private final String namespace;
    private final String topLevelObject;

    @JsonCreator
    public XmlSchema(@JsonProperty("xsdContent") String xsdContent,
                     @JsonProperty("namespace") String namespace,
                     @JsonProperty("topLevelObject") String topLevelObject) {
        this.xsdContent = xsdContent;
        this.namespace = namespace;
        this.topLevelObject = topLevelObject;
    }

    public String getXsdContent() {
        return xsdContent;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getTopLevelObject() {
        return topLevelObject;
    }

}
