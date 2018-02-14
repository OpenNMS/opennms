/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.core.db;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.opennmsDataSources.ConnectionPool;
import org.opennms.netmgt.config.opennmsDataSources.DataSourceConfiguration;
import org.opennms.netmgt.config.opennmsDataSources.JdbcDataSource;

/**
 * <p>
 * This is the class used to load the OpenNMS database configuration
 * from the opennms-datasources.xml.</p>
 *
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 */
public final class DataSourceConfigurationFactory {

	private final DataSourceConfiguration m_dsc;

	public DataSourceConfigurationFactory(File fileName) {
		try {
			m_dsc = JaxbUtils.unmarshal(DataSourceConfiguration.class, fileName);
		} catch (Exception e) {
			throw new IllegalArgumentException("Could not unmarshal " + DataSourceConfiguration.class.getName(), e);
		}
	}

	public DataSourceConfigurationFactory(String fileName) {
		this(new File(fileName));
	}

	public DataSourceConfigurationFactory(InputStream fileInputStream) {
		try (Reader reader = new InputStreamReader(fileInputStream)) {
		    m_dsc = JaxbUtils.unmarshal(DataSourceConfiguration.class, reader);
		} catch (Exception e) {
			throw new IllegalArgumentException("Could not unmarshal " + DataSourceConfiguration.class.getName(), e);
		}
	}

	public ConnectionPool getConnectionPool() {
		return m_dsc.getConnectionPool();
	}
	
	public JdbcDataSource getJdbcDataSource(String name) {
		for (JdbcDataSource ds : m_dsc.getJdbcDataSource()) {
			if (ds.getName().equals(name)) {
				return ds;
			}
		}
		return null;
	}
}
