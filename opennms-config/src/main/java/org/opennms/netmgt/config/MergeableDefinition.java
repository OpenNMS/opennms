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
package org.opennms.netmgt.config;

import java.util.ArrayList;

import org.opennms.core.network.IPAddressRange;
import org.opennms.core.network.IPAddressRangeSet;
import org.opennms.netmgt.config.snmp.Definition;
import org.opennms.netmgt.config.snmp.Range;

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
        
        for (Range r : def.getRanges()) {
            m_configRanges.add(new IPAddressRange(r.getBegin(), r.getEnd()));
        }
        
        for(String s : def.getSpecifics()) {
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
     * @param eventDefinition a {@link org.opennms.netmgt.config.MergeableDefinition} object.
     */
    protected void mergeMatchingAttributeDef(MergeableDefinition eventDefinition)  {
        
        m_configRanges.addAll(eventDefinition.getAddressRanges());
        
        getConfigDef().setRanges(new ArrayList<Range>());
        getConfigDef().setSpecifics(new ArrayList<String>());
        
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
    public final Definition getConfigDef() {
        return m_snmpConfigDef;
    }
    
    private final <T> boolean areEquals(T object1, T object2) {
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
        		&& areEquals(getConfigDef().getWriteCommunity(), other.getConfigDef().getWriteCommunity())
                && areEquals(getConfigDef().getTTL(), other.getConfigDef().getTTL())
                && areEquals(getConfigDef().getProfileLabel(), other.getConfigDef().getProfileLabel());
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
        && !getConfigDef().hasSecurityLevel()
        && !getConfigDef().hasTTL();
    }


    void removeRanges(MergeableDefinition eventDefinition) {
        
        m_configRanges.removeAll(eventDefinition.getAddressRanges());

        getConfigDef().setRanges(new ArrayList<Range>());
        getConfigDef().setSpecifics(new ArrayList<String>());
        
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
        return getConfigDef().getRanges().size() < 1 && getConfigDef().getSpecifics().size() < 1;
    }

}
