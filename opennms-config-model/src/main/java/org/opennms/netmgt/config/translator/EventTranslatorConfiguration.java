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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;

@XmlRootElement(name = "event-translator-configuration")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("translator-configuration.xsd")
public class EventTranslatorConfiguration implements Serializable {
    private static final long serialVersionUID = 2L;

    /**
     * List of OpenNMS events for which the Event Translator 
     *  will subscribe for translation.
     */
    @XmlElementWrapper(name = "translation", required=false)
    @XmlElement(name="event-translation-spec", required=true)
    private List<EventTranslationSpec> m_eventTranslationSpecs = new ArrayList<>();

    public EventTranslatorConfiguration() {
    }

    public List<EventTranslationSpec> getEventTranslationSpecs() {
        return m_eventTranslationSpecs;
    }

    public void setEventTranslationSpecs(final List<EventTranslationSpec> eventTranslationSpecs) {
        if (eventTranslationSpecs == m_eventTranslationSpecs) return;
        m_eventTranslationSpecs.clear();
        if (eventTranslationSpecs != null) m_eventTranslationSpecs.addAll(eventTranslationSpecs);
    }

    public void addEventTranslationSpec(final EventTranslationSpec spec) {
        m_eventTranslationSpecs.add(spec);
    }

    public boolean removeEventTranslationSpec(final EventTranslationSpec spec) {
        return m_eventTranslationSpecs.remove(spec);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_eventTranslationSpecs);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof EventTranslatorConfiguration) {
            final EventTranslatorConfiguration that = (EventTranslatorConfiguration)obj;
            return Objects.equals(this.m_eventTranslationSpecs, that.m_eventTranslationSpecs);
        }
        return false;
    }
}
