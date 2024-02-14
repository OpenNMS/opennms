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
package org.opennms.netmgt.dao.jmx;

import org.opennms.core.xml.AbstractJaxbConfigDao;
import org.opennms.netmgt.config.jmx.JmxConfig;

/**
 * Implementation for config dao class.
 *
 * @author Christian Pape <Christian.Pape@informatik.hs-fulda.de>
 */
public class JmxConfigDaoJaxb extends AbstractJaxbConfigDao<JmxConfig, JmxConfig> implements JmxConfigDao {
    /**
     * Default constructor
     */
    public JmxConfigDaoJaxb() {
        super(JmxConfig.class, "Jmx Configuration");
    }

    /**
     * Returns the loaded config object.
     *
     * @return the current config object
     */
    @Override
    public JmxConfig getConfig() {
        if (getContainer() != null) {
            return getContainer().getObject();
        }
        return null;
    }

    /**
     * Used to transform the config object to a custom representation. This method is not modified in this class, it just
     * returns the config object itself.
     *
     * @param jaxbConfig a config object.
     * @return a custom object
     */
    @Override
    public JmxConfig translateConfig(JmxConfig jaxbConfig) {
        return jaxbConfig;
    }
}
