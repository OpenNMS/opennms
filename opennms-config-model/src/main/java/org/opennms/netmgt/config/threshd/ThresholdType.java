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
package org.opennms.netmgt.config.threshd;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="type")
@XmlEnum
public enum ThresholdType {
    @XmlEnumValue("high")
    HIGH("high"),
    @XmlEnumValue("low")
    LOW("low"),
    @XmlEnumValue("relativeChange")
    RELATIVE_CHANGE("relativeChange"),
    @XmlEnumValue("absoluteChange")
    ABSOLUTE_CHANGE("absoluteChange"),
    @XmlEnumValue("rearmingAbsoluteChange")
    REARMING_ABSOLUTE_CHANGE("rearmingAbsoluteChange");
    
    private String m_enumName;

    ThresholdType(final String enumName) {
        m_enumName = enumName;
    }
    
    public String getEnumName() {
        return m_enumName;
    }

    public static ThresholdType forName(final String name) {
        for (final ThresholdType type : ThresholdType.values()) {
            if (name.equalsIgnoreCase(type.getEnumName())) {
                return type;
            }
        }
        return null;
    }
}
