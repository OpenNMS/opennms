/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.LogUtils;
import org.opennms.core.xml.CastorUtils;
import org.opennms.netmgt.config.snmpAsset.adapter.AssetField;
import org.opennms.netmgt.config.snmpAsset.adapter.SnmpAssetAdapterConfiguration;

/**
 * <p>Abstract RancidAdapterConfigManager class.</p>
 *
 * @author <a href="mailto:antonio@openms.it">Antonio Russo</a>
 * @author <a href="mailto:brozow@openms.org">Mathew Brozowski</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @version $Id: $
 */
public class SnmpAssetAdapterConfigManager implements SnmpAssetAdapterConfig {
    private final ReadWriteLock m_globalLock = new ReentrantReadWriteLock();
    private final Lock m_readLock = m_globalLock.readLock();
    private final Lock m_writeLock = m_globalLock.writeLock();
    
	private long m_lastModified;

    /**
     * The config class loaded from the config file
     */
    private SnmpAssetAdapterConfiguration m_config;

    /**
     * <p>Constructor for RancidAdapterConfigManager.</p>
     */
    public SnmpAssetAdapterConfigManager() {
    }

	/**
	 * <p>Constructor for RancidAdapterConfigManager.</p>
	 *
	 * @author <a href="mailto:antonio@opennms.org">Antonio Russo</a>
	 * @param reader a {@link java.io.InputStream} object.
	 * @param verifyServer a boolean.
	 * @throws org.exolab.castor.xml.MarshalException if any.
	 * @throws org.exolab.castor.xml.ValidationException if any.
	 * @throws java.io.IOException if any.
	 * @param serverName a {@link java.lang.String} object.
	 */
	public SnmpAssetAdapterConfigManager(final long lastModified, final InputStream reader) throws MarshalException, ValidationException, IOException {
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
	 * @throws org.exolab.castor.xml.MarshalException if any.
	 * @throws org.exolab.castor.xml.ValidationException if any.
	 * @throws java.io.IOException if any.
	 */
	protected void reloadXML(final long lastModified, final InputStream reader) throws MarshalException, ValidationException, IOException {
	    getWriteLock().lock();
	    try {
    		m_config = CastorUtils.unmarshal(SnmpAssetAdapterConfiguration.class, reader);
    		m_lastModified = lastModified;
	    } finally {
	        getWriteLock().unlock();
	    }
	}

	/**
	 * <p>Update</p>
	 *
	 * @throws java.io.IOException if any.
	 * @throws org.exolab.castor.xml.MarshalException if any.
	 * @throws org.exolab.castor.xml.ValidationException if any.
	 */
    @Override
	public void update() throws IOException, MarshalException, ValidationException {
	    getWriteLock().lock();
	    try {
    	    final File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.SNMP_ASSET_ADAPTER_CONFIG_FILE_NAME);
    		final long lastModified = cfgFile.lastModified();
    		if (lastModified > m_lastModified) {
    		    LogUtils.debugf(this, "init: config file path: %s", cfgFile.getPath());
    			reloadXML(lastModified, new FileInputStream(cfgFile));
    			LogUtils.debugf(this, "init: finished loading config file: %s", cfgFile.getPath());
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
    		    LogUtils.debugf(this, "getAssetFieldsForAddress: SNMP sysoid was null for address %s, returning empty list", InetAddressUtils.str(address));
    			return new AssetField[0];
    		}
    
    		final List<AssetField> retval = new ArrayList<AssetField>();
    		for (final org.opennms.netmgt.config.snmpAsset.adapter.Package pkg : m_config.getPackageCollection()) {
    		    final String pkgSysoid = pkg.getPackageChoice().getSysoid();
    			final String pkgSysoidMask = pkg.getPackageChoice().getSysoidMask();
    			if (pkgSysoid != null) {
    				if (pkgSysoid.equals(sysoid)) {
    					retval.addAll(pkg.getAssetFieldCollection());
    				}
    			} else if (pkgSysoidMask != null) {
    				if (sysoid.startsWith(pkgSysoidMask)) {
    					retval.addAll(pkg.getAssetFieldCollection());
    				}
    			} else {
    			    LogUtils.warnf(this, "getAssetFieldsForAddress: Unexpected condition: both sysoid and sysoidMask are null on package %s", pkg.getName());
    			}
    		}
    		if (retval.size() == 0) {
    			LogUtils.debugf(this, "getAssetFieldsForAddress: Zero AssetField matches returned for %s with sysoid: %s", InetAddressUtils.str(address), sysoid);
    		}
    		return retval.toArray(new AssetField[0]);
	    } finally {
	        getReadLock().unlock();
	    }
	}
}
