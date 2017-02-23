/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.trapd;

import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.netmgt.snmp.TrapIdentity;

@XmlRootElement(name="trap-identity")
@XmlAccessorType(value= XmlAccessType.NONE)
public class TrapIdentityDTO {

    @XmlAttribute(name="generic", required=true)
    private int generic;

    @XmlAttribute(name="specific", required=true)
    private int specific;

    @XmlAttribute(name="enterprise-id", required=true)
    private String enterpriseId;

    // Default Constructor for JAXB
    public TrapIdentityDTO() {

    }

    public TrapIdentityDTO(TrapIdentity trapIdentity) {
        Objects.requireNonNull(trapIdentity);
        this.generic = trapIdentity.getGeneric();
        this.specific = trapIdentity.getSpecific();
        this.enterpriseId = trapIdentity.getEnterpriseId();
    }

    public int getGeneric() {
        return generic;
    }

    public void setGeneric(int generic) {
        this.generic = generic;
    }

    public int getSpecific() {
        return specific;
    }

    public void setSpecific(int specific) {
        this.specific = specific;
    }

    public String getEnterpriseId() {
        return enterpriseId;
    }

    public void setEnterpriseId(String enterpriseId) {
        this.enterpriseId = enterpriseId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TrapIdentityDTO that = (TrapIdentityDTO) o;
        return Objects.equals(generic, that.generic)
                && Objects.equals(specific, that.specific)
                && Objects.equals(enterpriseId, that.enterpriseId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(generic, specific, enterpriseId);
    }
}
