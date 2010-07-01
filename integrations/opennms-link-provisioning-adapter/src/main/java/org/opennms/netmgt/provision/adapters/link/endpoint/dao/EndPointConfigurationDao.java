package org.opennms.netmgt.provision.adapters.link.endpoint.dao;

import org.opennms.netmgt.provision.adapters.link.endpoint.EndPointTypeValidator;

/**
 * <p>EndPointConfigurationDao interface.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public interface EndPointConfigurationDao {

    /**
     * <p>getValidator</p>
     *
     * @return a {@link org.opennms.netmgt.provision.adapters.link.endpoint.EndPointTypeValidator} object.
     */
    public EndPointTypeValidator getValidator();
    
    /**
     * <p>save</p>
     *
     * @param validator a {@link org.opennms.netmgt.provision.adapters.link.endpoint.EndPointTypeValidator} object.
     */
    public void save(EndPointTypeValidator validator);

    /**
     * <p>getXsd</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getXsd();
    
}
