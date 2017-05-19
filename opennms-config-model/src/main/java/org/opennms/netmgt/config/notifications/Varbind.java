/*******************************************************************************
 * This file is part of OpenNMS(R).
 * 
 * Copyright (C) 2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.notifications;


import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

/**
 * The varbind element
 */
@XmlRootElement(name = "varbind")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("notifications.xsd")
public class Varbind implements java.io.Serializable {
    private static final long serialVersionUID = 2L;

    /**
     * The varbind element number
     */
    @XmlElement(name = "vbname", required = true)
    private String m_vbname;

    /**
     * The varbind element value
     */
    @XmlElement(name = "vbvalue", required = true)
    private String m_vbvalue;

    public Varbind() {
    }

    public String getVbname() {
        return m_vbname;
    }

    public void setVbname(final String vbname) {
        m_vbname = ConfigUtils.assertNotEmpty(vbname, "vbname");
    }

    public String getVbvalue() {
        return m_vbvalue;
    }

    public void setVbvalue(final String vbvalue) {
        m_vbvalue = ConfigUtils.assertNotEmpty(vbvalue, "vbvalue");
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_vbname, m_vbvalue);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof Varbind) {
            final Varbind that = (Varbind)obj;
            return Objects.equals(this.m_vbname, that.m_vbname)
                    && Objects.equals(this.m_vbvalue, that.m_vbvalue);
        }
        return false;
    }

}
