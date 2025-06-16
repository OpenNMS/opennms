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
package org.opennms.netmgt.config.jmx;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * Jaxb parameter element for storing key/value pairs in the jmx config.
 *
 * @author Christian Pape <Christian.Pape@informatik.hs-fulda.de>
 */
public class Parameter {
    private String m_key;
    private String m_value;

    @XmlAttribute(name = "key", required = true)
    public String getKey() {
        return m_key;
    }

    @XmlAttribute(name = "value", required = true)
    public String getValue() {
        return m_value;
    }

    public void setKey(String key) {
        this.m_key = key;
    }

    public void setValue(String value) {
        this.m_value = value;
    }
}
