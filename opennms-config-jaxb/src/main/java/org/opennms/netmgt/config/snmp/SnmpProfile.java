/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.snmp;

import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;

@XmlRootElement(name="profile")
@XmlAccessorType(XmlAccessType.NONE)
@ValidateUsing("snmp-config.xsd")
public class SnmpProfile extends Configuration {

    private static final long serialVersionUID = 6047134979704016780L;
    
    public SnmpProfile(Integer port,
                       Integer retry,
                       Integer timeout,
                       String readCommunity,
                       String writeCommunity,
                       String proxyHost,
                       String version,
                       Integer maxVarsPerPdu,
                       Integer maxRepetitions,
                       Integer maxRequestSize,
                       String securityName,
                       Integer securityLevel,
                       String authPassphrase,
                       String authProtocol,
                       String engineId,
                       String contextEngineId,
                       String contextName,
                       String privacyPassphrase,
                       String privacyProtocol,
                       String enterpriseId,
                       String label,
                       String filterExpression) {

        super(port, retry, timeout, readCommunity, writeCommunity, proxyHost, version, maxVarsPerPdu,
                maxRepetitions, maxRequestSize, securityName, securityLevel, authPassphrase, authProtocol,
                engineId, contextEngineId, contextName, privacyPassphrase, privacyProtocol, enterpriseId);
        this.filterExpression = filterExpression;
        this.label = label;
    }

    public SnmpProfile() {
    }

    @XmlElement(name="label")
    private String label;

    @XmlElement(name="filter")
    private String filterExpression;


    public String getFilterExpression() {
        return filterExpression;
    }

    public void setFilterExpression(String filterExpression) {
        this.filterExpression = filterExpression;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void visit(SnmpConfigVisitor visitor) {
        visitor.visitSnmpProfile(this);
        visitor.visitSnmpProfileFinished();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        SnmpProfile that = (SnmpProfile) o;
        return Objects.equals(filterExpression, that.filterExpression);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), filterExpression);
    }
}
