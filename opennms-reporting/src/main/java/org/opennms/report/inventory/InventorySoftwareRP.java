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

package org.opennms.report.inventory;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Objects;

/**
 * Class InventorySoftwareRP.
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "inventorySoftwareRP")
@XmlAccessorType(XmlAccessType.FIELD)
public class InventorySoftwareRP implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    @XmlElement(name = "type")
    private String type;

    @XmlElement(name = "version")
    private String version;

    public InventorySoftwareRP() {
    }

    /**
     * Returns the value of field 'type'.
     * 
     * @return the value of field 'Type'.
     */
    public String getType() {
        return this.type;
    }

    /**
     * Returns the value of field 'version'.
     * 
     * @return the value of field 'Version'.
     */
    public String getVersion() {
        return this.version;
    }

    /**
     * Sets the value of field 'type'.
     * 
     * @param type the value of field 'type'.
     */
    public void setType(final String type) {
        this.type = type;
    }

    /**
     * Sets the value of field 'version'.
     * 
     * @param version the value of field 'version'.
     */
    public void setVersion(final String version) {
        this.version = version;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof InventorySoftwareRP)) {
            return false;
        }
        InventorySoftwareRP castOther = (InventorySoftwareRP) other;
        return Objects.equals(type, castOther.type) && Objects.equals(version, castOther.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, version);
    }

}
