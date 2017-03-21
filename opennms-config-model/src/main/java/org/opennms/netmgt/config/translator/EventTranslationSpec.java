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

package org.opennms.netmgt.config.translator;


import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This defines the allowable translations for a given
 *  event uei
 *  
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "event-translation-spec")
@XmlAccessorType(XmlAccessType.FIELD)
public class EventTranslationSpec implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    @XmlAttribute(name = "uei", required = true)
    private String uei;

    /**
     * The list of event mappings for this event. The first
     *  mapping that matches the event is used to translate the
     *  event into a new event.
     *  
     */
    @XmlElement(name = "mappings", required = true)
    private Mappings mappings;

    public EventTranslationSpec() {
    }

    /**
     * Overrides the Object.equals method.
     * 
     * @param obj
     * @return true if the objects are equal.
     */
    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }
        
        if (obj instanceof EventTranslationSpec) {
            EventTranslationSpec temp = (EventTranslationSpec)obj;
            boolean equals = Objects.equals(temp.uei, uei)
                && Objects.equals(temp.mappings, mappings);
            return equals;
        }
        return false;
    }

    /**
     * Returns the value of field 'mappings'. The field 'mappings' has the
     * following description: The list of event mappings for this event. The first
     *  mapping that matches the event is used to translate the
     *  event into a new event.
     *  
     * 
     * @return the value of field 'Mappings'.
     */
    public Mappings getMappings() {
        return this.mappings;
    }

    /**
     * Returns the value of field 'uei'.
     * 
     * @return the value of field 'Uei'.
     */
    public String getUei() {
        return this.uei;
    }

    /**
     * Method hashCode.
     * 
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode() {
        int hash = Objects.hash(
            uei, 
            mappings);
        return hash;
    }

    /**
     * Sets the value of field 'mappings'. The field 'mappings' has the following
     * description: The list of event mappings for this event. The first
     *  mapping that matches the event is used to translate the
     *  event into a new event.
     *  
     * 
     * @param mappings the value of field 'mappings'.
     */
    public void setMappings(final Mappings mappings) {
        this.mappings = mappings;
    }

    /**
     * Sets the value of field 'uei'.
     * 
     * @param uei the value of field 'uei'.
     */
    public void setUei(final String uei) {
        this.uei = uei;
    }

}
