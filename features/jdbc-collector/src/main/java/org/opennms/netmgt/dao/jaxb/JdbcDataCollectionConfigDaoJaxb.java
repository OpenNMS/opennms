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
package org.opennms.netmgt.dao.jaxb;

import org.opennms.core.xml.AbstractJaxbConfigDao;
import org.opennms.netmgt.config.jdbc.JdbcDataCollection;
import org.opennms.netmgt.config.jdbc.JdbcDataCollectionConfig;
import org.opennms.netmgt.dao.JdbcDataCollectionConfigDao;

public class JdbcDataCollectionConfigDaoJaxb extends AbstractJaxbConfigDao<JdbcDataCollectionConfig, JdbcDataCollectionConfig> implements JdbcDataCollectionConfigDao {

    public JdbcDataCollectionConfigDaoJaxb() {
        super(JdbcDataCollectionConfig.class, "JDBC Data Collection Configuration");
    }

    @Override
    public JdbcDataCollection getDataCollectionByName(String name) {
        JdbcDataCollectionConfig jdcc = getContainer().getObject();
        for (JdbcDataCollection dataCol : jdcc.getJdbcDataCollections()) {
            if(dataCol.getName().equals(name)) {
                return dataCol;
            }
        }

        return null;
    }

    @Override
    public JdbcDataCollection getDataCollectionByIndex(int idx) {
        JdbcDataCollectionConfig jdcc = getContainer().getObject();
        return jdcc.getJdbcDataCollections().get(idx);
    }

    @Override
    public JdbcDataCollectionConfig getConfig() {
        return getContainer().getObject();
    }

    @Override
    protected JdbcDataCollectionConfig translateConfig(JdbcDataCollectionConfig jaxbConfig) {
        return jaxbConfig;
    }

}
