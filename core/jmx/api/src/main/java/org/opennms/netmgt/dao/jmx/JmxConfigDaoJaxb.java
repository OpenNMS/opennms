package org.opennms.netmgt.dao.jmx;

import org.opennms.core.xml.AbstractJaxbConfigDao;
import org.opennms.netmgt.config.jmx.JmxConfig;

/**
 * Implementation for config dao class.
 *
 * @author Christian Pape <Christian.Pape@informatik.hs-fulda.de>
 */
public class JmxConfigDaoJaxb extends AbstractJaxbConfigDao<JmxConfig, JmxConfig> implements JmxConfigDao {
    /**
     * Default constructor
     */
    public JmxConfigDaoJaxb() {
        super(JmxConfig.class, "Jmx Configuration");
    }

    /**
     * Returns the loaded config object.
     *
     * @return the current config object
     */
    @Override
    public JmxConfig getConfig() {
        return getContainer().getObject();
    }

    /**
     * Used to transform the config object to a custom representation. This method is not modified in this class, it just
     * returns the config object itself.
     *
     * @param jaxbConfig a config object.
     * @return a custom object
     */
    @Override
    public JmxConfig translateConfig(JmxConfig jaxbConfig) {
        return jaxbConfig;
    }
}
