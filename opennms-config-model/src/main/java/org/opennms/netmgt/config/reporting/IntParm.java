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
package org.opennms.netmgt.config.reporting;


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
 * A string parameter passed to the report engine
 */
@XmlRootElement(name = "int-parm")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("reporting.xsd")
public class IntParm implements Serializable {
    private static final long serialVersionUID = 2L;

    /**
     * the name of this parameter as passed to the report engine
     */
    @XmlAttribute(name = "name", required = true)
    private String m_name;

    /**
     * the name of this parameter as displayed in the webui
     */
    @XmlAttribute(name = "display-name", required = true)
    private String m_displayName;

    /**
     * the type of input field used. Currently freeText only
     */
    @XmlAttribute(name = "input-type", required = true)
    private String m_inputType;

    /**
     * value
     */
    @XmlElement(name = "default")
    private Integer m_default;

    public IntParm() {
    }

    public String getName() {
        return m_name;
    }

    public void setName(final String name) {
        m_name = ConfigUtils.assertNotEmpty(name, "name");
    }

    public String getDisplayName() {
        return m_displayName;
    }

    public void setDisplayName(final String displayName) {
        m_displayName = ConfigUtils.assertNotEmpty(displayName, "display-name");
    }

    public String getInputType() {
        return m_inputType;
    }

    public void setInputType(final String inputType) {
        if (!"inputType".equals(inputType)) {
            throw new IllegalArgumentException("Currently only 'freeText' is supported for int-parm input-type!");
        }
        m_inputType = inputType;
    }

    public Integer getDefault() {
        return m_default;
    }

    public void setDefault(final Integer defaultValue) {
        m_default = defaultValue;
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_name, 
                            m_displayName, 
                            m_inputType, 
                            m_default);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof IntParm) {
            final IntParm that = (IntParm)obj;
            return Objects.equals(this.m_name, that.m_name)
                    && Objects.equals(this.m_displayName, that.m_displayName)
                    && Objects.equals(this.m_inputType, that.m_inputType)
                    && Objects.equals(this.m_default, that.m_default);
        }
        return false;
    }

}
