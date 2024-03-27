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


import java.io.Serializable;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

/**
 * Parameters to be used for threshold checking this
 *  service. Parameters are specfic to the service
 *  thresholder.
 */
@XmlRootElement(name = "parameter")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("thresholding.xsd")
public class Parameter implements Serializable {
    private static final long serialVersionUID = 2L;

    @XmlAttribute(name = "key", required = true)
    private String m_key;

    @XmlAttribute(name = "value", required = true)
    private String m_value;

    public Parameter() {
    }

    public String getKey() {
        return m_key;
    }

    public void setKey(final String key) {
        m_key = ConfigUtils.assertNotEmpty(key, "key");
    }

    public String getValue() {
        return m_value;
    }

    public void setValue(final String value) {
        m_value = ConfigUtils.assertNotEmpty(value, "value");
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_key, m_value);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof Parameter) {
            final Parameter that = (Parameter)obj;
            return Objects.equals(this.m_key, that.m_key)
                    && Objects.equals(this.m_value, that.m_value);
        }
        return false;
    }

}
