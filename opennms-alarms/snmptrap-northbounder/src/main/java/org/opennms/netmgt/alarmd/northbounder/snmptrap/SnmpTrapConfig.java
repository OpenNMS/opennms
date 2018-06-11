/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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
package org.opennms.netmgt.alarmd.northbounder.snmptrap;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.xml.event.Parm;

/**
 * The Class SnmpTrapConfig.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class SnmpTrapConfig {

    /** The enterprise id. */
    private String enterpriseId;

    /** The specific. */
    private int specific = -1;

    /** The generic. */
    private int generic = 6;

    /** The version. */
    private SnmpVersion version = SnmpVersion.V1;

    /** The community. */
    private String community;

    /** The host address. */
    private InetAddress hostAddress;

    /** The destination address. */
    private InetAddress destinationAddress;

    /** The destination port. */
    private int destinationPort = 0;

    /** The parameters. */
    private List<Parm> parameters = new ArrayList<>();

    /**
     * Gets the enterprise id.
     *
     * @return the enterprise id
     */
    public String getEnterpriseId() {
        return enterpriseId;
    }

    /**
     * Gets the specific.
     *
     * @return the specific
     */
    public int getSpecific() {
        return specific;
    }

    /**
     * Gets the generic.
     *
     * @return the generic
     */
    public int getGeneric() {
        return generic;
    }

    /**
     * Gets the SNMP version.
     *
     * @return the SNMP version
     */
    public SnmpVersion getVersion() {
        return version;
    }

    /**
     * Gets the community.
     *
     * @return the community
     */
    public String getCommunity() {
        return community;
    }

    /**
     * Gets the destination address.
     *
     * @return the destination address
     */
    public InetAddress getDestinationAddress() {
        return destinationAddress;
    }

    /**
     * Gets the destination port.
     *
     * @return the destination port
     */
    public int getDestinationPort() {
        return destinationPort;
    }

    /**
     * Gets the parameters.
     *
     * @return the parameters
     */
    public List<Parm> getParameters() {
        return parameters;
    }

    /**
     * Gets the parameter value.
     *
     * @param parameterName the parameter name
     * @return the parameter value
     */
    public String getParameterValue(String parameterName) {
        for (Parm p : getParameters()) {
            if (p.getParmName().equals(parameterName)) {
                return p.getValue().getContent();
            }
        }
        return null;
    }

    /**
     * Gets the host address.
     *
     * @return the host address
     */
    public InetAddress getHostAddress() {
        return hostAddress;
    }

    /**
     * Sets the enterprise id.
     *
     * @param enterpriseId the new enterprise id
     */
    public void setEnterpriseId(String enterpriseId) {
        this.enterpriseId = enterpriseId;
    }

    /**
     * Sets the specific.
     *
     * @param specific the new specific
     */
    public void setSpecific(int specific) {
        this.specific = specific;
    }

    /**
     * Sets the generic.
     *
     * @param generic the new generic
     */
    public void setGeneric(int generic) {
        this.generic = generic;
    }

    /**
     * Sets the SNMP version.
     *
     * @param version the new SNMP version
     */
    public void setVersion(SnmpVersion version) {
        this.version = version;
    }

    /**
     * Sets the community.
     *
     * @param community the new community
     */
    public void setCommunity(String community) {
        this.community = community;
    }

    /**
     * Sets the destination address.
     *
     * @param destinationAddress the new destination address
     */
    public void setDestinationAddress(InetAddress destinationAddress) {
        this.destinationAddress = destinationAddress;
    }

    /**
     * Sets the destination port.
     *
     * @param destinationPort the new destination port
     */
    public void setDestinationPort(int destinationPort) {
        this.destinationPort = destinationPort;
    }

    /**
     * Sets the parameters.
     *
     * @param parameters the new parameters
     */
    public void setParameters(List<Parm> parameters) {
        this.parameters = parameters;
    }

    /**
     * Sets the host address.
     *
     * @param hostAddress the new host address
     */
    public void setHostAddress(InetAddress hostAddress) {
        this.hostAddress = hostAddress;
    }

    /**
     * Checks for specific.
     *
     * @return true, if successful
     */
    public boolean hasSpecific() {
        return specific != -1;
    }

    /**
     * Checks for generic.
     *
     * @return true, if successful
     */
    public boolean hasGeneric() {
        return generic != -1;
    }

    /**
     * Adds the parameter.
     *
     * @param parameterName the parameter name
     * @param parameterValue the parameter value
     * @param parameterType the parameter type
     */
    public void addParameter(String parameterName, String parameterValue, String parameterType) {
        Parm p = new Parm(parameterName, parameterValue);
        p.getValue().setType(parameterType);
        parameters.add(p);
    }

    /**
     * Gets the SNMP agent configuration.
     *
     * @return the SNMP agent configuration
     */
    public SnmpAgentConfig getAgentConfig() {
        SnmpAgentConfig config = SnmpPeerFactory.getInstance().getAgentConfig(destinationAddress, version.intValue());
        if (destinationPort > 0) {
            config.setPort(destinationPort);
        }
        if ((version.isV1() || version.isV2()) && community != null) {
            config.setReadCommunity(community);
        }
        return config;
    }

    /**
     * Checks if is valid.
     *
     * @return true, if is valid
     */
    public boolean isValid() {
        if (version.isV1()) {
            for (Parm p : parameters) {
                if (p.getValue().getType().equals(VarbindType.TYPE_SNMP_COUNTER64.toString())) {
                    return false;
                }
            }

        }
        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SnmpTrapConfig[")
                .append("enterpriseId=").append(enterpriseId)
                .append(", generic=").append(generic)
                .append(", specific=").append(specific)
                .append(", version=").append(version)
                .append(", community=").append(community)
                .append(", hostAddress=").append(hostAddress.getHostAddress())
                .append(", destinationAddress=").append(destinationAddress.getHostAddress())
                .append(", destinationPort=").append(destinationPort)
                .append(", parameters={");
        for (int i=0; i<getParameters().size(); i++) {
            Parm p = getParameters().get(i);
            sb.append(p.getParmName()).append("(").append(p.getValue().getType()).append(")='").append(p.getValue().getContent()).append("'");
            if (i < getParameters().size() -1) {
                sb.append(", ");
            }
        }
        sb.append("}]");
        return sb.toString();
    }

}
