package org.opennms.netmgt.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.apache.commons.io.IOUtils;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.ConfigFileConstants;

/**
 * <p>SnmpAssetAdapterConfigFactory</p>
 */
public class SnmpAssetAdapterConfigFactory {

	/**
	 * Singleton instance of configuration that this factory provides.
	 */
	private final SnmpAssetAdapterConfigManager m_config;

	public SnmpAssetAdapterConfigFactory() throws MarshalException, ValidationException, IOException {
	    final File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.SNMP_ASSET_ADAPTER_CONFIG_FILE_NAME);
		LogUtils.debugf(this, "init: config file path: %s", cfgFile.getPath());
		final InputStream reader = new FileInputStream(cfgFile);
		m_config = new SnmpAssetAdapterConfigManager(cfgFile.lastModified(), reader);
		IOUtils.closeQuietly(reader);
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
	 * @throws java.io.IOException if any.
	 * @throws org.exolab.castor.xml.MarshalException if any.
	 * @throws org.exolab.castor.xml.ValidationException if any.
	 */
	public void reload() throws IOException, MarshalException, ValidationException {
		m_config.update();
	}

	/**
	 * <p>saveXml</p>
	 *
	 * @param xml a {@link java.lang.String} object.
	 * @throws java.io.IOException if any.
	 */
	protected void save(final String xml) throws IOException {
	    m_config.getWriteLock().lock();
	    try {
    		if (xml != null) {
    		    final long timestamp = System.currentTimeMillis();
    			final File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.SNMP_ASSET_ADAPTER_CONFIG_FILE_NAME);
    			LogUtils.debugf(this, "saveXml: saving config file at %d: %s", timestamp, cfgFile.getPath());
    			final Writer fileWriter = new OutputStreamWriter(new FileOutputStream(cfgFile), "UTF-8");
    			fileWriter.write(xml);
    			fileWriter.flush();
    			fileWriter.close();
    			LogUtils.debugf(this, "saveXml: finished saving config file: %s", cfgFile.getPath());
    		}
	    } finally {
	        m_config.getWriteLock().unlock();
	    }
	}

	/**
	 * Return the singleton instance of this factory.
	 *
	 * @return The current factory instance.
	 * @throws IOException 
	 * @throws ValidationException 
	 * @throws MarshalException 
	 * @throws java.lang.IllegalStateException
	 *             Thrown if the factory has not yet been initialized.
	 */
	public SnmpAssetAdapterConfig getInstance() {
	    m_config.getReadLock().lock();
	    try {
	        return m_config;
	    } finally {
	        m_config.getReadLock().unlock();
	    }
	}
}
