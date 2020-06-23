/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
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
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.wsmanAsset.adapter.AssetField;
import org.opennms.netmgt.config.wsmanAsset.adapter.WsManAssetAdapterConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WsManAssetAdapterConfigManager implements WsManAssetAdapterConfig {
    private static final Logger LOG = LoggerFactory.getLogger(WsManAssetAdapterConfigManager.class);
    private final ReadWriteLock m_globalLock = new ReentrantReadWriteLock();
    private final Lock m_readLock = m_globalLock.readLock();
    private final Lock m_writeLock = m_globalLock.writeLock();
    
    private long m_lastModified;

    private WsManAssetAdapterConfiguration m_config;

    public WsManAssetAdapterConfigManager() {
    }

    public WsManAssetAdapterConfigManager(final long lastModified, final InputStream reader) throws IOException {
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
     */
    protected void reloadXML(final long lastModified, final InputStream stream) throws IOException {
        getWriteLock().lock();
        try(final Reader reader = new InputStreamReader(stream)) {
            m_config = JaxbUtils.unmarshal(WsManAssetAdapterConfiguration.class, reader);
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
            final File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.WSMAN_ASSET_ADAPTER_CONFIG_FILE_NAME);
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
     * @return a {@link org.opennms.netmgt.config.snmpAsset.adapter.WsManAssetAdapterConfiguration} object.
     */
    protected WsManAssetAdapterConfiguration getConfiguration() {
        getReadLock().lock();
        try {
            return m_config;
        } finally {
            getReadLock().unlock();
        }
    }

    /**
     * Returns all {@link AssetField} objects that are in packages that match the specified
     * Vendor precisely. This relies on the WsManDetector issuing an Identify and populating
     * the Vendor and Product fields.
     */
    @Override
    public AssetField[] getAssetFieldsForAddress(final InetAddress address, final String vendor) {
        getReadLock().lock();
        
        try {
            if (vendor == null) {
                LOG.debug("getAssetFieldsForAddress: WSMAN vendor field was null for address {}, returning empty list", InetAddressUtils.str(address));
                return new AssetField[0];
            }
    
            final List<AssetField> retval = new ArrayList<>();
            for (final org.opennms.netmgt.config.wsmanAsset.adapter.Package pkg : m_config.getPackages()) {
                final String pkgVendor = pkg.getVendor();
                if (pkgVendor != null) {
                    if (pkgVendor.equals(vendor)) {
                        retval.addAll(pkg.getAssetFields());
                    }
                }
            }
            if (retval.isEmpty()) {
                LOG.debug("getAssetFieldsForAddress: Zero AssetField matches returned for {} with vendor: {}", InetAddressUtils.str(address), vendor);
            }
            return retval.toArray(new AssetField[0]);
        } finally {
            getReadLock().unlock();
        }
    }
}
