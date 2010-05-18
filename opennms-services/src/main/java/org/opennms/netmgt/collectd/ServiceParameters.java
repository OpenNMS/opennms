//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2006 Aug 15: Change log message to be more intuitive, IMHO. - dj@opennms.org
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//

package org.opennms.netmgt.collectd;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

import org.apache.log4j.Category;
import org.opennms.core.utils.ParameterMap;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.snmp.SnmpAgentConfig;

public class ServiceParameters {
    
    Map<String, String> m_parameters;

    public ServiceParameters(Map<String, String> parameters) {
        m_parameters = parameters;
    }
    
    public Map<String, String> getParameters() {
        return m_parameters;
    }

    public String toString() {
        return "domain: " + getDomain() + ", "
        + "storeByNodeID: " + getStoreByNodeID() + ", "
        + "storeByIfAlias: " + getStoreByIfAlias() + ", "
        + "storFlagOverride: " + getStorFlagOverride() + ", "
        + "ifAliasComment: " + getIfAliasComment();
    }

    String getDomain() {
        return ParameterMap.getKeyedString(getParameters(), "domain",
        		"default");
    }

    String getStoreByNodeID() {
        return ParameterMap.getKeyedString(getParameters(),
        		"storeByNodeID", "normal");
    }

    String getStoreByIfAlias() {
        return ParameterMap.getKeyedString(getParameters(),
        		"storeByIfAlias", "false");
    }

    String getStorFlagOverride() {
        return ParameterMap.getKeyedString(getParameters(),
        		"storFlagOverride", "false");
    }

    String getIfAliasComment() {
        return ParameterMap.getKeyedString(getParameters(),
        		"ifAliasComment", null);
    }

    boolean aliasesEnabled() {
        return getStoreByIfAlias().equals("true");
    }

    boolean overrideStorageFlag() {
        return !getStorFlagOverride().equals("false");
    }

    void logIfAliasConfig() {
    	log().info(this.toString());
    }

    private ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }
    
    boolean forceStoreByAlias(String alias) {
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

    String getCollectionName() {
        //icky hard coded old names; we need to handle some old cases where configs might be not yet updated, but they should
        // still work
        if(getParameters().containsKey("collection")) {
            return getParameters().get("collection");
        } else if(getParameters().containsKey("http-collection")) {
            return getParameters().get("http-collection");
        } else if(getParameters().containsKey("nsclient-collection")) {
            return getParameters().get("nsclient-collection");
        } else if(m_parameters.containsKey("wmi-collection")) {
            return m_parameters.get("wmi-collection");
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

    int getSnmpPort(int current) {
        return ParameterMap.getKeyedInteger(getParameters(), "port", current);
    }

    int getSnmpRetries(int current) {
        return ParameterMap.getKeyedInteger(getParameters(), "retry", current);
    }

    int getSnmpTimeout(int current) {
        return ParameterMap.getKeyedInteger(getParameters(), "timeout", current);
    }

    String getSnmpReadCommunity(String current) {
        String readCommunity = ParameterMap.getKeyedString(getParameters(), "read-community", null);
        if (readCommunity == null) {
            // incase someone is using an ancient config file
            readCommunity = ParameterMap.getKeyedString(m_parameters, "readCommunity", current);
        }
        return readCommunity;
    }

    String getSnmpWriteCommunity(String current) {
        return ParameterMap.getKeyedString(getParameters(), "write-community", current);
    }

    InetAddress getSnmpProxyFor(InetAddress current) {
        String address = ParameterMap.getKeyedString(getParameters(), "proxy-host", null);
        if (address != null) {
            try {
                return InetAddress.getByName(address);
            } catch (UnknownHostException e) {
                log().error("determineProxyHost: Problem converting proxy host string to InetAddress", e);
            }
        }
        return current;
    }

    int getSnmpVersion(int current) {
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

    int getSnmpMaxVarsPerPdu(int current) {
        return ParameterMap.getKeyedInteger(getParameters(), "max-vars-per-pdu", current);
    }

    int getSnmpMaxRepetitions(int current) {
        int maxRepetitions = ParameterMap.getKeyedInteger(m_parameters, "max-repetitions", -1);
        if (maxRepetitions == -1) {
            // incase someone is using an ancient config file
            maxRepetitions = ParameterMap.getKeyedInteger(m_parameters, "maxRepetitions", current);
        }
        return maxRepetitions;
    }

    int getSnmpMaxRequestSize(int current) {
        return ParameterMap.getKeyedInteger(getParameters(), "max-request-size", current);
    }

    String getSnmpSecurityName(String current) {
        return ParameterMap.getKeyedString(getParameters(), "security-name", current);
    }

    String getSnmpAuthPassPhrase(String current) {
        return ParameterMap.getKeyedString(getParameters(), "auth-passphrase", current);
    }

    String getSnmpAuthProtocol(String current) {
        return ParameterMap.getKeyedString(getParameters(), "auth-protocol", current);
    }

    String getSnmpPrivPassPhrase(String current) {
        return ParameterMap.getKeyedString(getParameters(), "privacy-passphrase", current);
    }

    String getSnmpPrivProtocol(String current) {
        return ParameterMap.getKeyedString(getParameters(), "privacy-protocol", current);
    }

}
