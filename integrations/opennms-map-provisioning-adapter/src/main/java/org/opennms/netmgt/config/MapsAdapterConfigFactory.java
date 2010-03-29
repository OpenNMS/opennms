package org.opennms.netmgt.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.log4j.Category;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.ConfigFileConstants;

public class MapsAdapterConfigFactory extends MapsAdapterConfigManager {

    /**
     * The singleton instance of this factory
     */
    private static MapsAdapterConfigFactory m_singleton = null;

    /**
     * This member is set to true if the configuration file has been loaded.
     */
    private static boolean m_loaded = false;
    
    /**
     * Loaded version
     */
    private long m_currentVersion = -1L;

    
    /**
     * constructor constructor
     * 
     * @exception java.io.IOException
     *                Thrown if the specified config file cannot be read
     * @exception org.exolab.castor.xml.MarshalException
     *                Thrown if the file does not conform to the schema.
     * @exception org.exolab.castor.xml.ValidationException
     *                Thrown if the contents do not match the required schema.
     */
    public MapsAdapterConfigFactory(long currentVersion, Reader reader, String localServer, boolean verifyServer) throws MarshalException, ValidationException, IOException {
        super(reader, localServer, verifyServer);
        m_currentVersion = currentVersion;
    }


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
        if (m_loaded) {
            // init already called - return
            // to reload, reload() will need to be called
            return;
        }
        OpennmsServerConfigFactory.init();
        OpennmsServerConfigFactory onmsSvrConfig = OpennmsServerConfigFactory.getInstance();

        File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.MAPS_ADAPTER_CONFIG_FILE_NAME);

        logStatic().debug("init: config file path: " + cfgFile.getPath());

        Reader reader = new InputStreamReader(new FileInputStream(cfgFile), "UTF-8");
        MapsAdapterConfigFactory config = new MapsAdapterConfigFactory(cfgFile.lastModified(), reader,onmsSvrConfig.getServerName(),onmsSvrConfig.verifyServer());
        reader.close();
        setInstance(config);

    }
    
    private static Category logStatic() {
        return ThreadCategory.getInstance(MapsAdapterConfigFactory.class);
    }

    /**
     * Reload the config from the default config file
     * 
     * @exception java.io.IOException
     *                Thrown if the specified config file cannot be read/loaded
     * @exception org.exolab.castor.xml.MarshalException
     *                Thrown if the file does not conform to the schema.
     * @exception org.exolab.castor.xml.ValidationException
     *                Thrown if the contents do not match the required schema.
     */
    public static synchronized void reload() throws IOException, MarshalException, ValidationException {
        init();
        getInstance().update();
    }
        
    protected synchronized void saveXml(String xml) throws IOException {
        if (xml != null) {
            long timestamp = System.currentTimeMillis();
            File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.MAPS_ADAPTER_CONFIG_FILE_NAME);
            logStatic().debug("saveXml: saving config file at "+timestamp+": " + cfgFile.getPath());
            FileWriter fileWriter = new FileWriter(cfgFile);
            fileWriter.write(xml);
            fileWriter.flush();
            fileWriter.close();
            logStatic().debug("saveXml: finished saving config file: " + cfgFile.getPath());
        }
    }

    /**
     * Return the singleton instance of this factory.
     * 
     * @return The current factory instance.
     * 
     * @throws java.lang.IllegalStateException
     *             Thrown if the factory has not yet been initialized.
     */
    public static synchronized MapsAdapterConfigFactory getInstance() {
        if (!m_loaded) {
            throw new IllegalStateException("The factory has not been initialized");
        }
        return m_singleton;
    }
    
    private static void setInstance(MapsAdapterConfigFactory instance) {
        m_singleton = instance;
        m_loaded = true;
    }

    public synchronized void update() throws IOException, MarshalException, ValidationException {

        File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.MAPS_ADAPTER_CONFIG_FILE_NAME);
        if (cfgFile.lastModified() > m_currentVersion) {
            m_currentVersion = cfgFile.lastModified();
            logStatic().debug("init: config file path: " + cfgFile.getPath());
            reloadXML(new InputStreamReader(new FileInputStream(cfgFile), "UTF-8"));
            logStatic().debug("init: finished loading config file: " + cfgFile.getPath());
        }
    }

}
