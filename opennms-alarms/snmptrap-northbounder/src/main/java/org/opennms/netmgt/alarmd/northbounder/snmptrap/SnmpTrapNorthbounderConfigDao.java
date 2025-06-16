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
package org.opennms.netmgt.alarmd.northbounder.snmptrap;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.xml.AbstractJaxbConfigDao;
import org.opennms.core.xml.JaxbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class SnmpTrapNorthbounderConfigDao.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class SnmpTrapNorthbounderConfigDao extends AbstractJaxbConfigDao<SnmpTrapNorthbounderConfig, SnmpTrapNorthbounderConfig> {

    /** The Constant LOG. */
    public static final Logger LOG = LoggerFactory.getLogger(SnmpTrapNorthbounderConfigDao.class);

    /** The configuration directory. */
    private File m_configDirectory;

    /**
     * Instantiates a new SNMP Trap northbounder configuration DAO.
     */
    public SnmpTrapNorthbounderConfigDao() {
        super(SnmpTrapNorthbounderConfig.class, "Config for SNMP Trap Northbounder");
    }

    /* (non-Javadoc)
     * @see org.opennms.core.xml.AbstractJaxbConfigDao#translateConfig(java.lang.Object)
     */
    @Override
    protected SnmpTrapNorthbounderConfig translateConfig(SnmpTrapNorthbounderConfig config) {
        for (SnmpTrapSink sink : config.getSnmpTrapSinks()) {
            if (sink.getImportMappings() != null) {
                for (String link : sink.getImportMappings()) {
                    File configFile = new File(getConfigDirectory(), link);
                    if (configFile.exists()) {
                        try {
                            LOG.debug("Parsing file {}", configFile);
                            SnmpTrapMappingGroup group = JaxbUtils.unmarshal(SnmpTrapMappingGroup.class, configFile);
                            sink.addMappingGroup(group);
                        } catch (Exception e) {
                            LOG.error("Can't parse {}", link, e);
                        }
                    }
                }
            }
        }
        return config;
    }

    /**
     * Gets the configuration directory.
     *
     * @return the configuration directory
     */
    private File getConfigDirectory() {
        if (m_configDirectory == null) {
            final StringBuilder sb = new StringBuilder(ConfigFileConstants.getHome());
            sb.append(File.separator);
            sb.append("etc");
            sb.append(File.separator);
            m_configDirectory = new File(sb.toString());
        }
        return m_configDirectory;
    }

    /**
     * Gets the SNMP Trap northbounder configuration.
     *
     * @return the configuration object
     */
    public SnmpTrapNorthbounderConfig getConfig() {
        return getContainer().getObject();
    }

    /**
     * Reload.
     */
    public void reload() {
        getContainer().reload();
    }

    /**
     * Save.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void save() throws IOException {
        SnmpTrapNorthbounderConfig cfg = getConfig();
        cfg.getSnmpTrapSinks().forEach(s -> s.cleanMappingGroups());
        JaxbUtils.marshal(cfg, new FileWriter(getConfigResource().getFile()));
    }

}
