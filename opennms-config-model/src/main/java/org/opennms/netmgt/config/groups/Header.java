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

package org.opennms.netmgt.config.groups;


import java.io.Serializable;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;

@XmlRootElement(name = "header")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("users.xsd")
public class Header implements Serializable {
    private static final long serialVersionUID = 1L;

    @XmlElement(name = "rev", required = true)
    private String m_rev;

    /**
     * Creation time in the 'dow mon dd hh:mm:ss zzz yyyy'
     *  format.
     */
    @XmlElement(name = "created", required = true)
    private String m_created;

    /**
     * Monitoring station? This is seemingly
     *  unused.
     */
    @XmlElement(name = "mstation", required = true)
    private String m_mstation;

    public Header() {
    }

    public Header(final String rev, final String created, final String mstation) {
        m_rev = rev;
        m_created = created;
        m_mstation = mstation;
    }

    public String getRev() {
        return m_rev;
    }

    public void setRev(final String rev) {
        m_rev = rev;
    }

    public String getCreated() {
        return m_created;
    }

    public void setCreated(final String created) {
        m_created = created;
    }

    public String getMStation() {
        return m_mstation;
    }

    public void setMStation(final String mstation) {
        m_mstation = mstation;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            m_rev, 
            m_created, 
            m_mstation);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }
        
        if (obj instanceof Header) {
            final Header temp = (Header)obj;
            return Objects.equals(temp.m_rev, m_rev)
                && Objects.equals(temp.m_created, m_created)
                && Objects.equals(temp.m_mstation, m_mstation);
        }
        return false;
    }

    @Override
    public String toString() {
        return "Header[rev=" + m_rev + ", created=" + m_created
                + ", mstation=" + m_mstation + "]";
    }

}
