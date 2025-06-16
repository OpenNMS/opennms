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
package org.opennms.netmgt.collection.api;

import java.net.InetAddress;
import java.util.Map;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.ParameterMap;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>ServiceParameters class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class ServiceParameters {
    private static final Logger LOG = LoggerFactory.getLogger(ServiceParameters.class);
    
    public static enum ParameterName {
        DOMAIN("domain"),
        STOREBYNODEID("storeByNodeID"),
        STOREBYIFALIAS("storeByIfAlias"),
        STORFLAGOVERRIDE("storFlagOverride"),
        IFALIASCOMMENT("ifAliasComment"),
        COLLECTION("collection"),
        @Deprecated
        HTTP_COLLECTION("http-collection"),
        @Deprecated
        NSCLIENT_COLLECTION("nsclient-collection"),
        @Deprecated
        WMI_COLLECTION("wmi-collection"),
        PORT("port"),
        RETRY("retry"),
        RETRIES("retries"),
        TIMEOUT("timeout"),
        READ_COMMUNITY("read-community"),
        @Deprecated
        READCOMMUNITY("readCommunity"),
        WRITE_COMMUNITY("write-community"),
        PROXY_HOST("proxy-host"),
        VERSION("version"),
        MAX_VARS_PER_PDU("max-vars-per-pdu"),
        MAX_REPETITIONS("max-repetitions"),
        @Deprecated
        MAXREPETITIONS("maxRepetitions"),
        MAX_REQUEST_SIZE("max-request-size"),
        SECURITY_NAME("security-name"),
        AUTH_PASSPHRASE("auth-passphrase"),
        AUTH_PROTOCOL("auth-protocol"),
        PRIVACY_PASSPHRASE("privacy-passphrase"),
        PRIVACY_PROTOCOL("privacy-protocol"),

        // JMX-specific parameters
        USE_MBEAN_NAME_FOR_RRDS("use-mbean-name-for-rrds"),
        FRIENDLY_NAME("friendly-name"),

        PACKAGE_NAME("packageName"),

        // Service level parameters set by collectd
        SERVICE("SERVICE"),
        SERVICE_INTERVAL("SERVICE_INTERVAL"),
        TTL("ttl");

        private final String m_value;

        private ParameterName(String value) {
            m_value = value;
        }

        @Override
        public String toString() {
            return m_value;
        }
    }

    private final Map<String, Object> m_parameters;

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
    @Override
    public String toString() {
        return "domain: " + getDomain() + ", "
        + "storeByNodeID: " + getStoreByNodeID() + ", "
        + "storeByIfAlias: " + getStoreByIfAlias() + ", "
        + "storeFlagOverride: " + getStorFlagOverride() + ", "
        + "ifAliasComment: " + getIfAliasComment();
    }

    public String getDomain() {
        return ParameterMap.getKeyedString(getParameters(), ParameterName.DOMAIN.toString(),
        		"default");
    }

    public String getStoreByNodeID() {
        return ParameterMap.getKeyedString(getParameters(),
        		ParameterName.STOREBYNODEID.toString(), "normal");
    }

    public String getStoreByIfAlias() {
        return ParameterMap.getKeyedString(getParameters(),
        		ParameterName.STOREBYIFALIAS.toString(), "false");
    }

    public String getStorFlagOverride() {
        return ParameterMap.getKeyedString(getParameters(),
        		ParameterName.STORFLAGOVERRIDE.toString(), "false");
    }

    public String getIfAliasComment() {
        return ParameterMap.getKeyedString(getParameters(),
        		ParameterName.IFALIASCOMMENT.toString(), null);
    }

    public boolean aliasesEnabled() {
        return getStoreByIfAlias().equals("true");
    }

    public boolean overrideStorageFlag() {
        return !getStorFlagOverride().equals("false");
    }

    public void logIfAliasConfig() {
	LOG.info("logIfAliasConfig: {}", this);
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
            return ParameterMap.getKeyedString(getParameters(), ParameterName.COLLECTION.toString(), "default");
        } else if(getParameters().containsKey("http-collection")) {
            return ParameterMap.getKeyedString(getParameters(), ParameterName.HTTP_COLLECTION.toString(), "default");
        } else if(getParameters().containsKey("nsclient-collection")) {
            return ParameterMap.getKeyedString(getParameters(), ParameterName.NSCLIENT_COLLECTION.toString(), "default");
        } else if(m_parameters.containsKey("wmi-collection")) {
            return ParameterMap.getKeyedString(getParameters(), ParameterName.WMI_COLLECTION.toString(), "default");
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
        return ParameterMap.getKeyedInteger(getParameters(), ParameterName.PORT.toString(), current);
    }

    public int getSnmpRetries(int current) {
        return ParameterMap.getKeyedInteger(getParameters(), ParameterName.RETRY.toString(), current);
    }

    public int getSnmpTimeout(int current) {
        return ParameterMap.getKeyedInteger(getParameters(), ParameterName.TIMEOUT.toString(), current);
    }

    public String getSnmpReadCommunity(String current) {
        String readCommunity = ParameterMap.getKeyedString(getParameters(), ParameterName.READ_COMMUNITY.toString(), null);
        if (readCommunity == null) {
            // incase someone is using an ancient config file
            readCommunity = ParameterMap.getKeyedString(m_parameters, ParameterName.READCOMMUNITY.toString(), current);
        }
        return readCommunity;
    }

    public String getSnmpWriteCommunity(String current) {
        return ParameterMap.getKeyedString(getParameters(), ParameterName.WRITE_COMMUNITY.toString(), current);
    }

    public InetAddress getSnmpProxyFor(InetAddress current) {
        String address = ParameterMap.getKeyedString(getParameters(), ParameterName.PROXY_HOST.toString(), null);
        InetAddress addr = null;
        if (address != null) {
        	addr = InetAddressUtils.addr(address);
        	if (addr == null) {
			LOG.error("determineProxyHost: Problem converting proxy host string to InetAddress");
            }
        }
        return addr == null? current : addr;
    }

    public int getSnmpVersion(int current) {
        String version = ParameterMap.getKeyedString(getParameters(), ParameterName.VERSION.toString(), null);
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
        return ParameterMap.getKeyedInteger(getParameters(), ParameterName.MAX_VARS_PER_PDU.toString(), current);
    }

    public int getSnmpMaxRepetitions(int current) {
        int maxRepetitions = ParameterMap.getKeyedInteger(m_parameters, ParameterName.MAX_REPETITIONS.toString(), -1);
        if (maxRepetitions == -1) {
            // in case someone is using an ancient config file
            maxRepetitions = ParameterMap.getKeyedInteger(m_parameters, ParameterName.MAXREPETITIONS.toString(), current);
        }
        return maxRepetitions;
    }

    public int getSnmpMaxRequestSize(int current) {
        return ParameterMap.getKeyedInteger(getParameters(), ParameterName.MAX_REQUEST_SIZE.toString(), current);
    }

    public String getSnmpSecurityName(String current) {
        return ParameterMap.getKeyedString(getParameters(), ParameterName.SECURITY_NAME.toString(), current);
    }

    public String getSnmpAuthPassPhrase(String current) {
        return ParameterMap.getKeyedString(getParameters(), ParameterName.AUTH_PASSPHRASE.toString(), current);
    }

    public String getSnmpAuthProtocol(String current) {
        return ParameterMap.getKeyedString(getParameters(), ParameterName.AUTH_PROTOCOL.toString(), current);
    }

    public String getSnmpPrivPassPhrase(String current) {
        return ParameterMap.getKeyedString(getParameters(), ParameterName.PRIVACY_PASSPHRASE.toString(), current);
    }

    public String getSnmpPrivProtocol(String current) {
        return ParameterMap.getKeyedString(getParameters(), ParameterName.PRIVACY_PROTOCOL.toString(), current);
    }

    public String getPackageName() {
        return ParameterMap.getKeyedString(getParameters(), ParameterName.PACKAGE_NAME.toString(), "unknown");
    }

    public Long getTTL() {
        Long ttl = ParameterMap.getLongValue(ParameterName.TTL.toString(), getParameters().get(ParameterName.TTL.toString()), null);
        if (ttl == null) {
            ttl = getServiceInterval();
        }
        return ttl;
    }

    public Long getServiceInterval() {
        // We use getKeyedString() instead of getKeyedLong() to avoid modifying the parameter map
        final String intervalAsStr = ParameterMap.getKeyedString(getParameters(), ParameterName.SERVICE_INTERVAL.toString(), null);
        if (intervalAsStr == null) {
            return null;
        }

        try {
            return Long.parseLong(intervalAsStr);
        } catch (NumberFormatException nfe) {
            // If an interval is set, it must be a valid long
            throw new IllegalArgumentException("Invalid interval " + intervalAsStr, nfe);
        }
    }
}
