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
