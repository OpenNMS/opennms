/*******************************************************************************
 * This file is part of OpenNMS(R).
 * 
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
 * 
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 * 
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 * 
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *     http://www.gnu.org/licenses/
 * 
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config.reporting;


import java.io.Serializable;
import java.util.Arrays;
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
@XmlRootElement(name = "string-parm")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("reporting.xsd")
public class StringParm implements Serializable {
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
     * the type of input field used. Allows the webUI to use pre-filled 
     *  drop-down boxes (reportCategorySelector) or freeText
     */
    @XmlAttribute(name = "input-type", required = true)
    private String m_inputType;

    /**
     * value
     */
    @XmlElement(name = "default")
    private String m_default;

    public StringParm() {
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
        m_inputType = ConfigUtils.assertOnlyContains(ConfigUtils.assertNotEmpty(inputType, "input-type"), Arrays.asList("reportCategorySelector", "freeText"), "input-type");
    }

    public String getDefault() {
        return m_default;
    }

    public void setDefault(final String defaultValue) {
        m_default = ConfigUtils.assertNotEmpty(defaultValue, "default");
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

        if (obj instanceof StringParm) {
            final StringParm that = (StringParm)obj;
            return Objects.equals(this.m_name, that.m_name)
                    && Objects.equals(this.m_displayName, that.m_displayName)
                    && Objects.equals(this.m_inputType, that.m_inputType)
                    && Objects.equals(this.m_default, that.m_default);
        }
        return false;
    }

}
