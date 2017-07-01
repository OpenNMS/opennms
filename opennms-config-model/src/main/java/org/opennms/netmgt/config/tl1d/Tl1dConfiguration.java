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

package org.opennms.netmgt.config.tl1d;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;

@XmlRootElement(name = "tl1d-configuration")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("tl1d-configuration.xsd")
public class Tl1dConfiguration implements Serializable {
    private static final long serialVersionUID = 2L;

    @XmlElement(name = "tl1-element")
    private List<Tl1Element> m_tl1Elements = new ArrayList<>();

    public List<Tl1Element> getTl1Elements() {
        return m_tl1Elements;
    }

    public void setTl1Elements(final List<Tl1Element> tl1Elements) {
        if (tl1Elements == m_tl1Elements) return;
        m_tl1Elements.clear();
        if (tl1Elements != null) m_tl1Elements.addAll(tl1Elements);
    }

    public void addTl1Element(final Tl1Element tl1Element) {
        m_tl1Elements.add(tl1Element);
    }

    public boolean removeTl1Element(final Tl1Element tl1Element) {
        return m_tl1Elements.remove(tl1Element);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_tl1Elements);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof Tl1dConfiguration) {
            final Tl1dConfiguration that = (Tl1dConfiguration)obj;
            return Objects.equals(this.m_tl1Elements, that.m_tl1Elements);
        }
        return false;
    }

}
