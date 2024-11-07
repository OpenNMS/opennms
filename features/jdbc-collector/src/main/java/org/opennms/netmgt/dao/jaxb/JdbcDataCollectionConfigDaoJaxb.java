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

import org.opennms.core.xml.AbstractMergingJaxbConfigDao;
import org.opennms.netmgt.config.jdbc.JdbcDataCollection;
import org.opennms.netmgt.config.jdbc.JdbcDataCollectionConfig;
import org.opennms.netmgt.dao.JdbcDataCollectionConfigDao;

import java.nio.file.Paths;

public class JdbcDataCollectionConfigDaoJaxb extends AbstractMergingJaxbConfigDao<JdbcDataCollectionConfig, JdbcDataCollectionConfig> implements JdbcDataCollectionConfigDao {

    public JdbcDataCollectionConfigDaoJaxb() {
        super(JdbcDataCollectionConfig.class, "JDBC Data Collection Configuration",
                Paths.get("etc", "jdbc-datacollection-config.xml"),
                Paths.get("etc", "jdbc-datacollection.d"));
    }

    @Override
    public JdbcDataCollection getDataCollectionByName(String name) {
        JdbcDataCollectionConfig jdcc = getObject();
        for (JdbcDataCollection dataCol : jdcc.getJdbcDataCollections()) {
            if(dataCol.getName().equals(name)) {
                return dataCol;
            }
        }
        return null;
    }

    @Override
    public JdbcDataCollectionConfig mergeConfigs(JdbcDataCollectionConfig source, JdbcDataCollectionConfig target) {
        if (target == null) {
            target = new JdbcDataCollectionConfig();
        }
        return target.merge(source);
    }

    @Override
    public JdbcDataCollection getDataCollectionByIndex(int idx) {
        JdbcDataCollectionConfig jdcc = getObject();
        return jdcc.getJdbcDataCollections().get(idx);
    }

    @Override
    public JdbcDataCollectionConfig getConfig() {
        return getObject();
    }

    @Override
    public JdbcDataCollectionConfig translateConfig(JdbcDataCollectionConfig jaxbConfig) {
        return jaxbConfig;
    }

}
