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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.opennms.core.xml.ValidateUsing;

/**
 * The Mask for event configuration: The mask contains one
 *  or more 'maskelements' which uniquely identify an event.
 */
@XmlRootElement(name="mask")
@XmlAccessorType(XmlAccessType.NONE)
@ValidateUsing("eventconf.xsd")
@XmlType(propOrder={"m_maskElements", "m_varbinds"})
public class Mask implements Serializable {
    private static final long serialVersionUID = 2L;

    /**
     * The mask element
     */
    @XmlElement(name="maskelement", required=true)
    private List<Maskelement> m_maskElements = new ArrayList<>();

    /**
     * The varbind element
     */
    @XmlElement(name="varbind")
    private List<Varbind> m_varbinds = new ArrayList<>();


    public List<Maskelement> getMaskelements() {
        return m_maskElements;
    }

    public void setMaskelements(final List<Maskelement> elements) {
        if (m_maskElements == elements) return;
        m_maskElements.clear();
        if (elements != null) m_maskElements.addAll(elements);
    }

    public void addMaskelement(final Maskelement element) {
        m_maskElements.add(element);
    }

    public boolean removeMaskelement(final Maskelement element) {
        return m_maskElements.remove(element);
    }

    public List<Varbind> getVarbinds() {
        return m_varbinds;
    }

    public void setVarbinds(final List<Varbind> varbinds) {
        if (m_varbinds == varbinds) return;
        m_varbinds.clear();
        if (varbinds != null) m_varbinds.addAll(varbinds);
    }

    public void addVarbind(final Varbind varbind) {
        m_varbinds.add(varbind);
    }

    public boolean removeVarbind(final Varbind varbind) {
        return m_varbinds.remove(varbind);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_maskElements, m_varbinds);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof Mask) {
            final Mask that = (Mask) obj;
            return Objects.equals(this.m_maskElements, that.m_maskElements) &&
                    Objects.equals(this.m_varbinds, that.m_varbinds);
        }
        return false;
    }

    public EventMatcher constructMatcher() {
        final EventMatcher[] matchers = new EventMatcher[m_maskElements.size()+m_varbinds.size()];
        int index = 0;
        for(final Maskelement maskElement : m_maskElements) {
            matchers[index] = maskElement.constructMatcher();
            index++;
        }

        for(final Varbind varbind : m_varbinds) {
            matchers[index] = varbind.constructMatcher();
            index++;
        }

        return EventMatchers.and(matchers);
    }

    public Maskelement getMaskElement(final String mename) {
        for(final Maskelement element : m_maskElements) {
            if (mename.equals(element.getMename())) {
                return element;
            }
        }
        return null;
    }

    public List<String> getMaskElementValues(final String mename) {
        final Maskelement element = getMaskElement(mename);
        return element == null ? null : element.getMevalues();
    }


}
