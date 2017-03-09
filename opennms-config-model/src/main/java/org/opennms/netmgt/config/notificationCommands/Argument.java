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

package org.opennms.netmgt.config.notificationCommands;


import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class Argument.
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "argument")
@XmlAccessorType(XmlAccessType.FIELD)
public class Argument implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    @XmlAttribute(name = "streamed", required = true)
    private String streamed;

    @XmlElement(name = "substitution")
    private String substitution;

    @XmlElement(name = "switch")
    private String _switch;

    public Argument() {
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
        
        if (obj instanceof Argument) {
            Argument temp = (Argument)obj;
            boolean equals = Objects.equals(temp.streamed, streamed)
                && Objects.equals(temp.substitution, substitution)
                && Objects.equals(temp._switch, _switch);
            return equals;
        }
        return false;
    }

    /**
     * Returns the value of field 'streamed'.
     * 
     * @return the value of field 'Streamed'.
     */
    public String getStreamed() {
        return this.streamed;
    }

    /**
     * Returns the value of field 'substitution'.
     * 
     * @return the value of field 'Substitution'.
     */
    public String getSubstitution() {
        return this.substitution;
    }

    /**
     * Returns the value of field 'switch'.
     * 
     * @return the value of field 'Switch'.
     */
    public String getSwitch() {
        return this._switch;
    }

    /**
     * Method hashCode.
     * 
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode() {
        int hash = Objects.hash(
            streamed, 
            substitution, 
            _switch);
        return hash;
    }

    /**
     * Sets the value of field 'streamed'.
     * 
     * @param streamed the value of field 'streamed'.
     */
    public void setStreamed(final String streamed) {
        this.streamed = streamed;
    }

    /**
     * Sets the value of field 'substitution'.
     * 
     * @param substitution the value of field 'substitution'.
     */
    public void setSubstitution(final String substitution) {
        this.substitution = substitution;
    }

    /**
     * Sets the value of field 'switch'.
     * 
     * @param _switch
     * @param switch the value of field 'switch'.
     */
    public void setSwitch(final String _switch) {
        this._switch = _switch;
    }

}
