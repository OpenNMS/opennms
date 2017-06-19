/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2017 The OpenNMS Group, Inc.
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
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.xml.eventconf;

import java.io.Serializable;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

/**
 * This element is used for converting event 
 *  varbind value in static decoded string.
 */
@XmlRootElement(name="decode")
@XmlAccessorType(XmlAccessType.NONE)
@ValidateUsing("eventconf.xsd")
public class Decode implements Serializable {
    private static final long serialVersionUID = 2L;

    @XmlAttribute(name="varbindvalue", required=true)
    private String m_varbindvalue;

    @XmlAttribute(name="varbinddecodedstring",required=true)
    private String m_varbinddecodedstring;

    public String getVarbindvalue() {
        return m_varbindvalue;
    }

    public void setVarbindvalue(final String varbindvalue) {
        m_varbindvalue = ConfigUtils.assertNotNull(varbindvalue, "varbindvalue").intern();
    }

    public String getVarbinddecodedstring() {
        return m_varbinddecodedstring;
    }

    public void setVarbinddecodedstring(final String varbinddecodedstring) {
        m_varbinddecodedstring = ConfigUtils.assertNotNull(varbinddecodedstring, "varbinddecodedstring").intern();
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_varbindvalue, m_varbinddecodedstring);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof Decode) {
            final Decode that = (Decode) obj;
            return Objects.equals(this.m_varbindvalue, that.m_varbindvalue) &&
                    Objects.equals(this.m_varbinddecodedstring, that.m_varbinddecodedstring);
        }
        return false;
    }

    @Override
    public String toString() {
        return m_varbindvalue + '=' + m_varbinddecodedstring;
    }

}
