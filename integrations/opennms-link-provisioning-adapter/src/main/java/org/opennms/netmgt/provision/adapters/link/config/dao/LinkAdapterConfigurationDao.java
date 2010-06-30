package org.opennms.netmgt.provision.adapters.link.config.dao;

import java.util.Set;

import org.opennms.netmgt.provision.adapters.link.config.linkadapter.LinkPattern;

/**
 * <p>LinkAdapterConfigurationDao interface.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public interface LinkAdapterConfigurationDao {

    /**
     * <p>getPatterns</p>
     *
     * @return a {@link java.util.Set} object.
     */
    public Set<LinkPattern> getPatterns();
    
    /**
     * <p>setPatterns</p>
     *
     * @param patterns a {@link java.util.Set} object.
     */
    public void setPatterns(Set<LinkPattern> patterns);
    
    /**
     * <p>saveCurrent</p>
     */
    public void saveCurrent();
    
}
