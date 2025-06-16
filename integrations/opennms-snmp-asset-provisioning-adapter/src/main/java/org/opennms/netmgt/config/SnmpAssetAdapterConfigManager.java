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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.snmpAsset.adapter.AssetField;
import org.opennms.netmgt.config.snmpAsset.adapter.SnmpAssetAdapterConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SnmpAssetAdapterConfigManager implements SnmpAssetAdapterConfig {
    private static final Logger LOG = LoggerFactory.getLogger(SnmpAssetAdapterConfigManager.class);
    private final ReadWriteLock m_globalLock = new ReentrantReadWriteLock();
    private final Lock m_readLock = m_globalLock.readLock();
    private final Lock m_writeLock = m_globalLock.writeLock();
    
	private long m_lastModified;

    /**
     * The config class loaded from the config file
     */
    private SnmpAssetAdapterConfiguration m_config;

    public SnmpAssetAdapterConfigManager() {
    }

	public SnmpAssetAdapterConfigManager(final long lastModified, final InputStream reader) throws IOException {
		reloadXML(lastModified, reader);
	}

    @Override
    public Lock getReadLock() {
        return m_readLock;
    }
    
    @Override
    public Lock getWriteLock() {
        return m_writeLock;
    }

	/**
	 * Synchronized so that we update the timestamp of the file and the contents
	 * simultaneously.
	 *
	 * @param reader a {@link java.io.InputStream} object.
	 * @throws java.io.IOException if any.
	 */
	protected void reloadXML(final long lastModified, final InputStream stream) throws IOException {
	    getWriteLock().lock();
	    try(final Reader reader = new InputStreamReader(stream)) {
    		m_config = JaxbUtils.unmarshal(SnmpAssetAdapterConfiguration.class, reader);
    		m_lastModified = lastModified;
	    } finally {
	        getWriteLock().unlock();
	    }
	}

	/**
	 * <p>Update</p>
	 *
	 * @throws java.io.IOException if any.
	 */
    @Override
	public void update() throws IOException {
	    getWriteLock().lock();
	    try {
    	    final File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.SNMP_ASSET_ADAPTER_CONFIG_FILE_NAME);
    		final long lastModified = cfgFile.lastModified();
    		if (lastModified > m_lastModified) {
    		    LOG.debug("init: config file path: {}", cfgFile.getPath());
    			reloadXML(lastModified, new FileInputStream(cfgFile));
    			LOG.debug("init: finished loading config file: {}", cfgFile.getPath());
    		}
	    } finally {
	        getWriteLock().unlock();
	    }
	}

	/**
	 * Return the configuration object.
	 *
	 * @return a {@link org.opennms.netmgt.config.snmpAsset.adapter.SnmpAssetAdapterConfiguration} object.
	 */
	protected SnmpAssetAdapterConfiguration getConfiguration() {
	    getReadLock().lock();
	    try {
	        return m_config;
	    } finally {
	        getReadLock().unlock();
	    }
	}

	/**
	 * Returns all {@link AssetField} objects that are in packages that match the specified
	 * sysoid precisely based on the <sysoid> tag or by starting with the content of the 
	 * <sysoidMask> tag.
	 * 
	 * TODO: Support matching based on IP address
	 */
    @Override
	public AssetField[] getAssetFieldsForAddress(final InetAddress address, final String sysoid) {
	    getReadLock().lock();
	    
	    try {
    		if (sysoid == null) {
    			// If the sysoid is null, we won't be able to fetch any SNMP attributes;
    			// return an empty list.
    		    LOG.debug("getAssetFieldsForAddress: SNMP sysoid was null for address {}, returning empty list", InetAddressUtils.str(address));
    			return new AssetField[0];
    		}
    
    		final List<AssetField> retval = new ArrayList<>();
    		for (final org.opennms.netmgt.config.snmpAsset.adapter.Package pkg : m_config.getPackages()) {
    		    final String pkgSysoid = pkg.getSysoid();
    			final String pkgSysoidMask = pkg.getSysoidMask();
    			if (pkgSysoid != null) {
    				if (pkgSysoid.equals(sysoid)) {
    					retval.addAll(pkg.getAssetFields());
    				}
    			} else if (pkgSysoidMask != null) {
    				if (sysoid.startsWith(pkgSysoidMask)) {
    					retval.addAll(pkg.getAssetFields());
    				}
    			} else {
    			    LOG.warn("getAssetFieldsForAddress: Unexpected condition: both sysoid and sysoidMask are null on package {}", pkg.getName());
    			}
    		}
    		if (retval.size() == 0) {
    			LOG.debug("getAssetFieldsForAddress: Zero AssetField matches returned for {} with sysoid: {}", InetAddressUtils.str(address), sysoid);
    		}
    		return retval.toArray(new AssetField[0]);
	    } finally {
	        getReadLock().unlock();
	    }
	}
}
