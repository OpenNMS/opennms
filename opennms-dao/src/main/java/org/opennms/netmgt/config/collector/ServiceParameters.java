/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.collector;

import java.net.InetAddress;
import java.util.Map;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.ParameterMap;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.snmp.SnmpAgentConfig;

/**
 * <p>ServiceParameters class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class ServiceParameters {
    
    Map<String, Object> m_parameters;

    /**
     * <p>Constructor for ServiceParameters.</p>
     *
     * @param parameters a {@link java.util.Map} object.
     */
    public ServiceParameters(Map<String, Object> parameters) {
        m_parameters = parameters;
    }
    
    /**
     * <p>getParameters</p>
     *
     * @return a {@link java.util.Map} object.
     */
    public Map<String, Object> getParameters() {
        return m_parameters;
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String toString() {
        return "domain: " + getDomain() + ", "
        + "storeByNodeID: " + getStoreByNodeID() + ", "
        + "storeByIfAlias: " + getStoreByIfAlias() + ", "
        + "storFlagOverride: " + getStorFlagOverride() + ", "
        + "ifAliasComment: " + getIfAliasComment();
    }

    public String getDomain() {
        return ParameterMap.getKeyedString(getParameters(), "domain",
        		"default");
    }

    public String getStoreByNodeID() {
        return ParameterMap.getKeyedString(getParameters(),
        		"storeByNodeID", "normal");
    }

    public String getStoreByIfAlias() {
        return ParameterMap.getKeyedString(getParameters(),
        		"storeByIfAlias", "false");
    }

    public String getStorFlagOverride() {
        return ParameterMap.getKeyedString(getParameters(),
        		"storFlagOverride", "false");
    }

    public String getIfAliasComment() {
        return ParameterMap.getKeyedString(getParameters(),
        		"ifAliasComment", null);
    }

    public boolean aliasesEnabled() {
        return getStoreByIfAlias().equals("true");
    }

    public boolean overrideStorageFlag() {
        return !getStorFlagOverride().equals("false");
    }

    public void logIfAliasConfig() {
    	log().info(this.toString());
    }

    private ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }
    
    public boolean forceStoreByAlias(String alias) {
    	if(alias == null || alias.equals("")) {
    		return false;
    	}
    	String comment = getIfAliasComment();
    	int si = -1;
    	if( comment != null && !comment.equals("")) {
            si = alias.indexOf(comment);
    	}
    	//if ifAlias begins with comment, return false
        if (si == 0) {
            return false;
        }
        return overrideStorageFlag();
    }

    public String getCollectionName() {
        //icky hard coded old names; we need to handle some old cases where configs might be not yet updated, but they should
        // still work
        if(getParameters().containsKey("collection")) {
            return ParameterMap.getKeyedString(getParameters(), "collection", "default");
        } else if(getParameters().containsKey("http-collection")) {
            return ParameterMap.getKeyedString(getParameters(), "http-collection", "default");
        } else if(getParameters().containsKey("nsclient-collection")) {
            return ParameterMap.getKeyedString(getParameters(), "nsclient-collection", "default");
        } else if(m_parameters.containsKey("wmi-collection")) {
            return ParameterMap.getKeyedString(getParameters(), "wmi-collection", "default");
        } else {
            return "default";
        }
        //Previous code:  we can return to this in time (maybe 1.6, or even 2.0) when all old
        // configs should be long gone
        //return ParameterMap.getKeyedString(getParameters(), "collection", "default");
    }

    /* (non-Javadoc)
     * Parameters corresponding to attributes from snmp-config
     */

    public int getSnmpPort(int current) {
        return ParameterMap.getKeyedInteger(getParameters(), "port", current);
    }

    public int getSnmpRetries(int current) {
        return ParameterMap.getKeyedInteger(getParameters(), "retry", current);
    }

    public int getSnmpTimeout(int current) {
        return ParameterMap.getKeyedInteger(getParameters(), "timeout", current);
    }

    public String getSnmpReadCommunity(String current) {
        String readCommunity = ParameterMap.getKeyedString(getParameters(), "read-community", null);
        if (readCommunity == null) {
            // incase someone is using an ancient config file
            readCommunity = ParameterMap.getKeyedString(m_parameters, "readCommunity", current);
        }
        return readCommunity;
    }

    public String getSnmpWriteCommunity(String current) {
        return ParameterMap.getKeyedString(getParameters(), "write-community", current);
    }

    public InetAddress getSnmpProxyFor(InetAddress current) {
        String address = ParameterMap.getKeyedString(getParameters(), "proxy-host", null);
        InetAddress addr = null;
        if (address != null) {
        	addr = InetAddressUtils.addr(address);
        	if (addr == null) {
        		log().error("determineProxyHost: Problem converting proxy host string to InetAddress");
            }
        }
        return addr == null? current : addr;
    }

    public int getSnmpVersion(int current) {
        String version = ParameterMap.getKeyedString(getParameters(), "version", null);
        if (version != null) {
            if (version.equals("v1")) {
                return SnmpAgentConfig.VERSION1;
            } else if (version.equals("v2c")) {
                return SnmpAgentConfig.VERSION2C;
            } else if (version.equals("v3")) {
                return SnmpAgentConfig.VERSION3;
            }
        }
        return current;
    }

    public int getSnmpMaxVarsPerPdu(int current) {
        return ParameterMap.getKeyedInteger(getParameters(), "max-vars-per-pdu", current);
    }

    public int getSnmpMaxRepetitions(int current) {
        int maxRepetitions = ParameterMap.getKeyedInteger(m_parameters, "max-repetitions", -1);
        if (maxRepetitions == -1) {
            // in case someone is using an ancient config file
            maxRepetitions = ParameterMap.getKeyedInteger(m_parameters, "maxRepetitions", current);
        }
        return maxRepetitions;
    }

    public int getSnmpMaxRequestSize(int current) {
        return ParameterMap.getKeyedInteger(getParameters(), "max-request-size", current);
    }

    public String getSnmpSecurityName(String current) {
        return ParameterMap.getKeyedString(getParameters(), "security-name", current);
    }

    public String getSnmpAuthPassPhrase(String current) {
        return ParameterMap.getKeyedString(getParameters(), "auth-passphrase", current);
    }

    public String getSnmpAuthProtocol(String current) {
        return ParameterMap.getKeyedString(getParameters(), "auth-protocol", current);
    }

    public String getSnmpPrivPassPhrase(String current) {
        return ParameterMap.getKeyedString(getParameters(), "privacy-passphrase", current);
    }

    public String getSnmpPrivProtocol(String current) {
        return ParameterMap.getKeyedString(getParameters(), "privacy-protocol", current);
    }

}
