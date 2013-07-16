/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config;

import org.opennms.netmgt.config.snmp.Definition;
import org.opennms.netmgt.config.snmp.Range;
import org.opennms.netmgt.model.discovery.IPAddressRange;
import org.opennms.netmgt.model.discovery.IPAddressRangeSet;

/**
 * This is a wrapper class for the Definition class from the config package.  Has the logic for 
 * comparing definitions, sorting child elements, etc.
 * 
 * @author <a href="mailto:david@opennms.org>David Hustace</a>
 *
 */
final class MergeableDefinition {
    
    /**
     * This field should remaining encapsulated for there is
     * synchronization in the getter.
     * 
     */
    private final Definition m_snmpConfigDef;
    private IPAddressRangeSet m_configRanges = new IPAddressRangeSet();
    
    /**
     * <p>Constructor for MergeableDefinition.</p>
     *
     * @param def a {@link org.opennms.netmgt.config.snmp.Definition} object.
     */
    public MergeableDefinition(Definition def) {
        m_snmpConfigDef = def;
        
        for (Range r : def.getRangeCollection()) {
            m_configRanges.add(new IPAddressRange(r.getBegin(), r.getEnd()));
        }
        
        for(String s : def.getSpecificCollection()) {
            m_configRanges.add(new IPAddressRange(s));
        }
    }
    
    public IPAddressRangeSet getAddressRanges() {
        return m_configRanges;
    }

    /**
     * This method is called when a definition is found in the config and
     * that has the same attributes as the params in the configureSNMP event and
     * the IP specific/range needs to be merged into the definition.
     *
     * @param eventDefefinition a {@link org.opennms.netmgt.config.MergeableDefinition} object.
     */
    protected void mergeMatchingAttributeDef(MergeableDefinition eventDefinition)  {
        
        m_configRanges.addAll(eventDefinition.getAddressRanges());
        
        getConfigDef().removeAllRange();
        getConfigDef().removeAllSpecific();
        
        for(IPAddressRange range : m_configRanges) {
            if (range.isSingleton()) {
                getConfigDef().addSpecific(range.getBegin().toUserString());
            } else {
                Range xmlRange = new Range();
                xmlRange.setBegin(range.getBegin().toUserString());
                xmlRange.setEnd(range.getEnd().toUserString());
                getConfigDef().addRange(xmlRange);
            }
            
        }        
    }
    
    /**
     * <p>getConfigDef</p>
     *
     * @return a {@link org.opennms.netmgt.config.snmp.Definition} object.
     */
    final public Definition getConfigDef() {
        return m_snmpConfigDef;
    }
    
    final private <T> boolean areEquals(T object1, T object2) {
    	return SnmpConfigManager.areEquals(object1, object2);
    }

    boolean matches(MergeableDefinition other) {
        return areEquals(getConfigDef().getReadCommunity(), other.getConfigDef().getReadCommunity())
                && areEquals(getConfigDef().getPort(), other.getConfigDef().getPort()) 
                && areEquals(getConfigDef().getRetry(), other.getConfigDef().getRetry())
                && areEquals(getConfigDef().getTimeout(), other.getConfigDef().getTimeout())
                && areEquals(getConfigDef().getVersion(), other.getConfigDef().getVersion())
                && areEquals(getConfigDef().getMaxRepetitions(), other.getConfigDef().getMaxRepetitions())
                && areEquals(getConfigDef().getMaxVarsPerPdu(), other.getConfigDef().getMaxVarsPerPdu())
        		&& areEquals(getConfigDef().getAuthPassphrase(), other.getConfigDef().getAuthPassphrase())
        		&& areEquals(getConfigDef().getAuthProtocol(), other.getConfigDef().getAuthProtocol())
        		&& areEquals(getConfigDef().getContextEngineId(), other.getConfigDef().getContextEngineId())
        		&& areEquals(getConfigDef().getContextName(), other.getConfigDef().getContextName())
        		&& areEquals(getConfigDef().getEngineId(), other.getConfigDef().getEngineId())
        		&& areEquals(getConfigDef().getEnterpriseId(), other.getConfigDef().getEnterpriseId())
        		&& areEquals(getConfigDef().getMaxRequestSize(), other.getConfigDef().getMaxRequestSize())
        		&& areEquals(getConfigDef().getPrivacyPassphrase(), other.getConfigDef().getPrivacyPassphrase())
        		&& areEquals(getConfigDef().getPrivacyProtocol(), other.getConfigDef().getPrivacyProtocol())
        		&& areEquals(getConfigDef().getProxyHost(), other.getConfigDef().getProxyHost())
        		&& areEquals(getConfigDef().getSecurityLevel(), other.getConfigDef().getSecurityLevel())
        		&& areEquals(getConfigDef().getSecurityName(), other.getConfigDef().getSecurityName()) 
        		&& areEquals(getConfigDef().getWriteCommunity(), other.getConfigDef().getWriteCommunity());
    }
    
    boolean isEmpty(String s) {
        return s == null || "".equals(s.trim());
    }
    
    /**
     * Returns true if the definition has no attributes (e.g. version, port, etc.) set. 
     * That means each range or specific matches the default values.
     * 
     * @return true if the definition has no attributes (e.g. version, port, etc.) set.
     */
    boolean isTrivial() {
        return isEmpty(getConfigDef().getReadCommunity()) 
        && isEmpty(getConfigDef().getVersion())
        && isEmpty(getConfigDef().getAuthPassphrase())
        && isEmpty(getConfigDef().getAuthProtocol())
        && isEmpty(getConfigDef().getContextEngineId())
        && isEmpty(getConfigDef().getContextName())
        && isEmpty(getConfigDef().getEngineId())
        && isEmpty(getConfigDef().getEnterpriseId())
        && isEmpty(getConfigDef().getPrivacyPassphrase())
        && isEmpty(getConfigDef().getPrivacyProtocol())
        && isEmpty(getConfigDef().getSecurityName())
        && isEmpty(getConfigDef().getWriteCommunity())
        && isEmpty(getConfigDef().getProxyHost())
        && !getConfigDef().hasPort()
        && !getConfigDef().hasRetry()
        && !getConfigDef().hasTimeout()
        && !getConfigDef().hasMaxRepetitions()
        && !getConfigDef().hasMaxRequestSize()
        && !getConfigDef().hasMaxVarsPerPdu()
        && !getConfigDef().hasSecurityLevel();
    }


    void removeRanges(MergeableDefinition eventDefinition) {
        
        m_configRanges.removeAll(eventDefinition.getAddressRanges());

        getConfigDef().removeAllRange();
        getConfigDef().removeAllSpecific();
        
        for(IPAddressRange r : m_configRanges) {
            if (r.isSingleton()) {
                getConfigDef().addSpecific(r.getBegin().toUserString());
            } else {
                Range xmlRange = new Range();
                xmlRange.setBegin(r.getBegin().toUserString());
                xmlRange.setEnd(r.getEnd().toUserString());
                getConfigDef().addRange(xmlRange);
            }
            
        }
    }

    /**
     * A definition is empty if there is no range and no specific defined.
     * @return true if the range count and specific count is 0.
     */
    boolean isEmpty() {
        return getConfigDef().getRangeCount() < 1 && getConfigDef().getSpecificCount() < 1;
    }

}
