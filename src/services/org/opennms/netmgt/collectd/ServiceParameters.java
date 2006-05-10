package org.opennms.netmgt.collectd;

import java.util.Map;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.utils.ParameterMap;

public class ServiceParameters {
    
    Map m_parameters;

    public ServiceParameters(Map parameters) {
        m_parameters = parameters;
    }
    
    public Map getParameters() {
        return m_parameters;
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
        if (aliasesEnabled()) {
        	log()
            .debug(
            		"domain:storeByNodeID:storeByIfAlias:"
            				+ "storFlagOverride:ifAliasComment = "
            				+ getDomain() + ":" + getStoreByNodeID() + ":"
            				+ getStoreByIfAlias() + ":"
            				+ getStorFlagOverride() + ":"
            				+ getIfAliasComment());
        }
    }

    private Category log() {
        return ThreadCategory.getInstance(getClass());
    }

    boolean forceStoreByAlias(String alias) {
        return overrideStorageFlag() && ((alias != null));
    }

}
