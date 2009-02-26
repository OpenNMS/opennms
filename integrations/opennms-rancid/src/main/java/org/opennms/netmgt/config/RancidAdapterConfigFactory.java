package org.opennms.netmgt.config;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.ConfigFileConstants;
import org.opennms.rancid.RancidNode;
import org.opennms.rancid.RancidNodeAuthentication;

public class RancidAdapterConfigFactory implements RancidAdapterConfig {

    /**
     * The singleton instance of this factory
     */
    private static RancidAdapterConfigFactory m_singleton = null;

    /**
     * This member is set to true if the configuration file has been loaded.
     */
    private static boolean m_loaded = false;
    /**
     * Load the config from the default config file and create the singleton
     * instance of this factory.
     * 
     * @exception java.io.IOException
     *                Thrown if the specified config file cannot be read
     * @exception org.exolab.castor.xml.MarshalException
     *                Thrown if the file does not conform to the schema.
     * @exception org.exolab.castor.xml.ValidationException
     *                Thrown if the contents do not match the required schema.
     */
    public static synchronized void init() throws IOException, MarshalException, ValidationException {
        /* TODO 
        if (m_loaded) {
            // init already called - return
            // to reload, reload() will need to be called
            return;
        }
        OpennmsServerConfigFactory.init();
        OpennmsServerConfigFactory onmsSvrConfig = OpennmsServerConfigFactory.getInstance();

        File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.RWS_CONFIG_FILE_NAME);

        logStatic().debug("init: config file path: " + cfgFile.getPath());

        FileReader reader = new FileReader(cfgFile);
        RWSConfigFactory config = new RWSConfigFactory(cfgFile.lastModified(), reader);
        reader.close();
        setInstance(config);

        */
        RancidAdapterConfigFactory config = new RancidAdapterConfigFactory();
        setInstance(config);

    }
    
    private static void setInstance(RancidAdapterConfigFactory instance) {
            m_singleton = instance;
            m_loaded = true;
        }

        

    /**
     * Return the singleton instance of this factory.
     * 
     * @return The current factory instance.
     * 
     * @throws java.lang.IllegalStateException
     *             Thrown if the factory has not yet been initialized.
     */
    public static synchronized RancidAdapterConfigFactory getInstance() {
        if (!m_loaded) {
            throw new IllegalStateException("The factory has not been initialized");
        }
        return m_singleton;
    }

 //   public String getGroup() {
        // TODO Create Configuration file to set group using packages
 //       return "laboratorio";
 //   }

    public RancidAdapterConfigFactory() {
    }

    public String getDefaultConnectionType() {
        // TODO Auto-generated method stub
        return "telnet";
    }
    
}
