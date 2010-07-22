//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005-2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2007 May 06: Eliminate a warning. - dj@opennms.org
// 2006 Apr 27: Added support for pathOutageEnabled
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.netmgt.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.ConfigFileConstants;
import org.opennms.netmgt.config.snmpAsset.adapter.AssetField;
import org.opennms.netmgt.config.snmpAsset.adapter.SnmpAssetAdapterConfiguration;
import org.opennms.netmgt.dao.castor.CastorUtils;

/**
 * <p>Abstract RancidAdapterConfigManager class.</p>
 *
 * @author <a href="mailto:antonio@openms.it">Antonio Russo</a>
 * @author <a href="mailto:brozow@openms.org">Mathew Brozowski</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @version $Id: $
 */
public class SnmpAssetAdapterConfigManager implements SnmpAssetAdapterConfig {

	private long m_lastModified;

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
	public SnmpAssetAdapterConfigManager(long lastModified, InputStream reader) throws MarshalException, ValidationException, IOException {
		reloadXML(lastModified, reader);
	}

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
	 * Synchronized so that we update the timestamp of the file and the contents
	 * simultaneously.
	 *
	 * @param reader a {@link java.io.InputStream} object.
	 * @throws org.exolab.castor.xml.MarshalException if any.
	 * @throws org.exolab.castor.xml.ValidationException if any.
	 * @throws java.io.IOException if any.
	 */
	protected synchronized void reloadXML(long lastModified, InputStream reader) throws MarshalException, ValidationException, IOException {
		m_config = CastorUtils.unmarshal(SnmpAssetAdapterConfiguration.class, reader);
		m_lastModified = lastModified;
	}

	/**
	 * <p>Update</p>
	 *
	 * @throws java.io.IOException if any.
	 * @throws org.exolab.castor.xml.MarshalException if any.
	 * @throws org.exolab.castor.xml.ValidationException if any.
	 */
	public void update() throws IOException, MarshalException, ValidationException {
		File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.SNMP_ASSET_ADAPTER_CONFIG_FILE_NAME);
		long lastModified = cfgFile.lastModified();
		if (lastModified > m_lastModified) {
			log().debug("init: config file path: " + cfgFile.getPath());
			reloadXML(lastModified, new FileInputStream(cfgFile));
			log().debug("init: finished loading config file: " + cfgFile.getPath());
		}
	}

	/**
	 * Return the configuration object.
	 *
	 * @return a {@link org.opennms.netmgt.config.snmpAsset.adapter.SnmpAssetAdapterConfiguration} object.
	 */
	protected SnmpAssetAdapterConfiguration getConfiguration() {
		return m_config;
	}

	protected static ThreadCategory log() {
		return ThreadCategory.getInstance(SnmpAssetAdapterConfigManager.class);
	}

	/**
	 * Returns all {@link AssetField} objects that are in packages that match the specified
	 * sysoid precisely based on the <sysoid> tag or by starting with the content of the 
	 * <sysoidMask> tag.
	 * 
	 * TODO: Support matching based on IP address
	 */
	public AssetField[] getAssetFieldsForAddress(InetAddress address, String sysoid) {
		if (sysoid == null) {
			// If the sysoid is null, we won't be able to fetch any SNMP attributes;
			// return an empty list.
			log().debug("getAssetFieldsForAddress: SNMP sysoid was null for address " + address.getHostAddress() + ", returning empty list");
			return new AssetField[0];
		}

		List<AssetField> retval = new ArrayList<AssetField>();
		for (org.opennms.netmgt.config.snmpAsset.adapter.Package pkg : m_config.getPackageCollection()) {
			String pkgSysoid = pkg.getPackageChoice().getSysoid();
			String pkgSysoidMask = pkg.getPackageChoice().getSysoidMask();
			if (pkgSysoid != null) {
				if (pkgSysoid.equals(sysoid)) {
					retval.addAll(pkg.getAssetFieldCollection());
				}
			} else if (pkgSysoidMask != null) {
				if (sysoid.startsWith(pkgSysoidMask)) {
					retval.addAll(pkg.getAssetFieldCollection());
				}
			} else {
				log().warn("getAssetFieldsForAddress: Unexpected condition: both sysoid and sysoidMask are null on package " + pkg.getName());
			}
		}
		if (log().isDebugEnabled()) {
			if (retval.size() == 0) {
				log().debug("getAssetFieldsForAddress: Zero AssetField matches returned for " + address.getHostAddress() + " with sysoid: " + sysoid);
			}
		}
		return retval.toArray(new AssetField[0]);
	}
}
