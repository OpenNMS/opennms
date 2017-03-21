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

package org.opennms.netmgt.config.syslogd;


import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * List of substrings or regexes that, when matched, signal
 *  that the message has sensitive contents and should
 *  therefore be hidden
 *  
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "hideMatch")
@XmlAccessorType(XmlAccessType.FIELD)public class HideMatch implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * The match expression
     */
    @XmlElement(name = "match", required = true)
    private org.opennms.netmgt.config.syslogd.Match match;

    public HideMatch() {
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
        
        if (obj instanceof HideMatch) {
            HideMatch temp = (HideMatch)obj;
            boolean equals = Objects.equals(temp.match, match);
            return equals;
        }
        return false;
    }

    /**
     * Returns the value of field 'match'. The field 'match' has the following
     * description: The match expression
     * 
     * @return the value of field 'Match'.
     */
    public org.opennms.netmgt.config.syslogd.Match getMatch() {
        return this.match;
    }

    /**
     * Method hashCode.
     * 
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode() {
        int hash = Objects.hash(
            match);
        return hash;
    }

    /**
     * Sets the value of field 'match'. The field 'match' has the following
     * description: The match expression
     * 
     * @param match the value of field 'match'.
     */
    public void setMatch(final org.opennms.netmgt.config.syslogd.Match match) {
        this.match = match;
    }

}
