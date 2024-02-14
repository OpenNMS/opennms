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
package org.opennms.netmgt.config.jdbc;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.opennms.core.utils.ConfigFileConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.core.xml.JaxbUtils;
import org.xml.sax.InputSource;

public class JdbcDataCollectionConfigFactory {
    private static final Logger LOG = LoggerFactory.getLogger(JdbcDataCollectionConfigFactory.class);

    private JdbcDataCollectionConfig m_jdbcDataCollectionConfig = null;
    
    public JdbcDataCollectionConfigFactory() {
        try {
            File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.JDBC_COLLECTION_CONFIG_FILE_NAME);
            LOG.debug("init: config file path: {}", cfgFile.getPath());
            InputStream reader = new FileInputStream(cfgFile);
            unmarshall(reader);
            reader.close();
        } catch(IOException e) {
            // TODO rethrow.
        }
    }
    
    public JdbcDataCollectionConfig unmarshall(InputStream configFile) {
        try {
            m_jdbcDataCollectionConfig = JaxbUtils.unmarshal(JdbcDataCollectionConfig.class, new InputSource(configFile));
            return m_jdbcDataCollectionConfig;
        } catch (Throwable e) {
            // TODO!!
            //throw new ForeignSourceRepositoryException("unable to access default foreign source resource", e);
        }
        return m_jdbcDataCollectionConfig;
    }

}
