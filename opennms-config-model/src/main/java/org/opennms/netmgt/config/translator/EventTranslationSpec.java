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

package org.opennms.netmgt.config.translator;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

/**
 * This defines the allowable translations for a given
 *  event uei
 */
@XmlRootElement(name = "event-translation-spec")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("translator-configuration.xsd")
public class EventTranslationSpec implements Serializable {
    private static final long serialVersionUID = 2L;

    @XmlAttribute(name = "uei", required = true)
    private String m_uei;

    /**
     * The list of event mappings for this event. The first
     *  mapping that matches the event is used to translate the
     *  event into a new event.
     *  
     */
    @XmlElementWrapper(name = "mappings", required = true)
    @XmlElement(name = "mapping")
    private List<Mapping> m_mappings = new ArrayList<>();

    public EventTranslationSpec() {
    }

    public String getUei() {
        return m_uei;
    }

    public void setUei(final String uei) {
        m_uei = ConfigUtils.assertNotEmpty(uei, "uei");
    }

    public List<Mapping> getMappings() {
        return m_mappings;
    }

    public void setMappings(final List<Mapping> mappings) {
        if (mappings == m_mappings) return;
        m_mappings.clear();
        if (mappings != null) m_mappings.addAll(mappings);
    }

    public void addMapping(final Mapping mapping) {
        m_mappings.add(mapping);
    }

    public boolean removeMapping(final Mapping mapping) {
        return m_mappings.remove(mapping);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_uei, m_mappings);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof EventTranslationSpec) {
            final EventTranslationSpec that = (EventTranslationSpec)obj;
            return Objects.equals(this.m_uei, that.m_uei)
                    && Objects.equals(this.m_mappings, that.m_mappings);
        }
        return false;
    }

}
