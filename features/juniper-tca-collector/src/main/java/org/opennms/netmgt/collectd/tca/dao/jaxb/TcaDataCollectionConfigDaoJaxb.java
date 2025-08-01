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
	protected TcaDataCollectionConfig translateConfig(TcaDataCollectionConfig config) {
		return config;
	}

}
