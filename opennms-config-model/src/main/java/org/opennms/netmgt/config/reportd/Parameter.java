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
package org.opennms.netmgt.config.reportd;


import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

@XmlRootElement(name = "parameter")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("reportd-configuration.xsd")
public class Parameter implements Serializable {
    private static final long serialVersionUID = 2L;

    @XmlAttribute(name = "name", required = true)
    private String m_name;

    @XmlAttribute(name = "value", required = true)
    private String m_value;

    @XmlAnyElement(lax=true)
    private Object m_anyObject;

    public Parameter() {
    }

    public String getName() {
        return m_name;
    }

    public void setName(final String name) {
        m_name = ConfigUtils.assertNotEmpty(name, "name");
    }

    public String getValue() {
        return m_value;
    }

    public void setValue(final String value) {
        m_value = ConfigUtils.assertNotNull(value, "value");
    }

    public Optional<Object> getAnyObject() {
        return Optional.ofNullable(m_anyObject);
    }

    public void setAnyObject(final Object anyObject) {
        m_anyObject = anyObject;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                            m_name, 
                            m_value, 
                            m_anyObject);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof Parameter) {
            final Parameter that = (Parameter)obj;
            return Objects.equals(this.m_name, that.m_name)
                    && Objects.equals(this.m_value, that.m_value)
                    && Objects.equals(this.m_anyObject, that.m_anyObject);
        }
        return false;
    }

}
