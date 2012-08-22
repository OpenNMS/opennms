/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collectd.tca.dao.jaxb;

import org.opennms.core.xml.AbstractJaxbConfigDao;
import org.opennms.netmgt.collectd.tca.config.TcaDataCollectionConfig;
import org.opennms.netmgt.collectd.tca.dao.TcaDataCollectionConfigDao;

/**
 * The Class TcaDataCollectionConfigDaoJaxb.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class TcaDataCollectionConfigDaoJaxb  extends AbstractJaxbConfigDao<TcaDataCollectionConfig,TcaDataCollectionConfig> implements TcaDataCollectionConfigDao {

	/**
	 * Instantiates a new tca data collection config dao jaxb.
	 */
	public TcaDataCollectionConfigDaoJaxb() {
		super(TcaDataCollectionConfig.class, "TCA Data Collection Configuration");
	}

	/* (non-Javadoc)
	 * @see org.opennms.netmgt.collectd.tca.dao.TcaDataCollectionConfigDao#getConfig()
	 */
	@Override
	public TcaDataCollectionConfig getConfig() {
		return getContainer().getObject();
	}

	/* (non-Javadoc)
	 * @see org.opennms.netmgt.dao.AbstractJaxbConfigDao#translateConfig(java.lang.Object)
	 */
	@Override
	protected TcaDataCollectionConfig translateConfig(TcaDataCollectionConfig castorConfig) {
		return castorConfig;
	}

}
