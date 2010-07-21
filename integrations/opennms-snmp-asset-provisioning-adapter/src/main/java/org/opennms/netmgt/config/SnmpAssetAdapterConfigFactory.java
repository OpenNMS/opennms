package org.opennms.netmgt.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ThreadCategory;
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
		File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.SNMP_ASSET_ADAPTER_CONFIG_FILE_NAME);
		log().debug("init: config file path: " + cfgFile.getPath());
		InputStream reader = new FileInputStream(cfgFile);
		m_config = new SnmpAssetAdapterConfigManager(cfgFile.lastModified(), reader);
		reader.close();
	}

	protected static ThreadCategory log() {
		return ThreadCategory.getInstance(SnmpAssetAdapterConfigFactory.class);
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
	public synchronized void reload() throws IOException, MarshalException, ValidationException {
		m_config.update();
	}

	/**
	 * <p>saveXml</p>
	 *
	 * @param xml a {@link java.lang.String} object.
	 * @throws java.io.IOException if any.
	 */
	protected synchronized void save(String xml) throws IOException {
		if (xml != null) {
			long timestamp = System.currentTimeMillis();
			File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.SNMP_ASSET_ADAPTER_CONFIG_FILE_NAME);
			log().debug("saveXml: saving config file at "+timestamp+": " + cfgFile.getPath());
			Writer fileWriter = new OutputStreamWriter(new FileOutputStream(cfgFile), "UTF-8");
			fileWriter.write(xml);
			fileWriter.flush();
			fileWriter.close();
			log().debug("saveXml: finished saving config file: " + cfgFile.getPath());
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
	public synchronized SnmpAssetAdapterConfig getInstance() {
		return m_config;
	}
}
