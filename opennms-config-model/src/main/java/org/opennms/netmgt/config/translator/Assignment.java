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
package org.opennms.netmgt.config.translator;


import java.io.Serializable;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

/**
 * An element representing an assignment to an attribute of the event
 */
@XmlRootElement(name = "assignment")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("translator-configuration.xsd")
public class Assignment implements Serializable {
    private static final long serialVersionUID = 2L;

    @XmlAttribute(name = "type", required = true)
    private String m_type;

    @XmlAttribute(name = "name", required = true)
    private String m_name;

    @XmlAttribute(name = "default", required = false)
    private String m_default;

    /**
     * An element representing a value to be used in a
     *  translation. 
     */
    @XmlElement(name = "value", required = true)
    private Value m_value;

    public Assignment() {
    }

    public String getType() {
        return m_type;
    }

    public void setType(final String type) {
        m_type = ConfigUtils.assertNotEmpty(type, "type");
    }

    public String getName() {
        return m_name;
    }

    public void setName(final String name) {
        m_name = ConfigUtils.assertNotEmpty(name, "name");
    }

    public Value getValue() {
        return m_value;
    }

    public void setValue(final Value value) {
        m_value = ConfigUtils.assertNotNull(value, "value");
    }

    public String getDefault() {
        return m_default;
    }

    public void setDefault(final String defaultValue) {
        this.m_default = ConfigUtils.assertNotNull(defaultValue, "default");
    }

    public boolean hasDefault() {
        return m_default != null;
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_type, m_name, m_default, m_value);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof Assignment) {
            final Assignment that = (Assignment)obj;
            return Objects.equals(this.m_type, that.m_type)
                    && Objects.equals(this.m_name, that.m_name)
                    && Objects.equals(this.m_default, that.m_default)
                    && Objects.equals(this.m_value, that.m_value);
        }
        return false;
    }

}
