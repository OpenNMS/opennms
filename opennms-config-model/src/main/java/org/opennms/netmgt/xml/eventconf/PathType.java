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
package org.opennms.netmgt.xml.eventconf;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="path")
@XmlEnum
public enum PathType {
    @XmlEnumValue("suppressDuplicates")
    SUPPRESS_DUPLICATES("suppressDuplicates"),
    @XmlEnumValue("cancellingEvent")
    CANCELLING_EVENT("cancellingEvent"),
    @XmlEnumValue("suppressAndCancel")
    SUPPRESS_AND_CANCEL("suppressAndCancel"),
    @XmlEnumValue("pathOutage")
    PATH_OUTAGE("pathOutage");

    private String m_value;

    private PathType(final String value) {
        m_value = value;
    }

    public static PathType fromString(final String v) {
        for (final PathType type : PathType.values()) {
            if (v.equalsIgnoreCase(type.toString())) {
                return type;
            }
        }
        return null;
    }

    public String toString() {
        return m_value;
    }
}
