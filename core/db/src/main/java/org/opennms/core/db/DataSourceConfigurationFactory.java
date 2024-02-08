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
