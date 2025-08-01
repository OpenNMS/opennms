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
package org.opennms.netmgt.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.opennms.core.utils.ConfigFileConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>SnmpAssetAdapterConfigFactory</p>
 */
public class SnmpAssetAdapterConfigFactory {
    private static final Logger LOG = LoggerFactory.getLogger(SnmpAssetAdapterConfigFactory.class);

	/**
	 * Singleton instance of configuration that this factory provides.
	 */
	private final SnmpAssetAdapterConfigManager m_config;

	public SnmpAssetAdapterConfigFactory() throws IOException {
	    final File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.SNMP_ASSET_ADAPTER_CONFIG_FILE_NAME);
		LOG.debug("init: config file path: {}", cfgFile.getPath());
		final InputStream reader = new FileInputStream(cfgFile);
		m_config = new SnmpAssetAdapterConfigManager(cfgFile.lastModified(), reader);
		IOUtils.closeQuietly(reader);
	}

	/**
	 * Reload the config from the default config file
	 *
	 * @exception java.io.IOException
	 *                Thrown if the specified config file cannot be read/loaded
	 * @throws java.io.IOException if any.
	 */
	public void reload() throws IOException {
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
    			LOG.debug("saveXml: saving config file at {}: {}", timestamp, cfgFile.getPath());
    			final Writer fileWriter = new OutputStreamWriter(new FileOutputStream(cfgFile), StandardCharsets.UTF_8);
    			fileWriter.write(xml);
    			fileWriter.flush();
    			fileWriter.close();
    			LOG.debug("saveXml: finished saving config file: {}", cfgFile.getPath());
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
