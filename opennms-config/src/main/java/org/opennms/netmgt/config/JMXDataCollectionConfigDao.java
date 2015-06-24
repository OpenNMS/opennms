/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config;

import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.xml.AbstractJaxbConfigDao;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.collectd.jmx.JmxCollection;
import org.opennms.netmgt.config.collectd.jmx.JmxDatacollectionConfig;
import org.opennms.netmgt.config.collectd.jmx.Mbeans;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;

import java.io.File;

/**
 * JAXB Based JMX Data Collection Config DAO
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class JMXDataCollectionConfigDao extends AbstractJaxbConfigDao<JmxDatacollectionConfig,JmxDatacollectionConfig> {

    public static final Logger LOG = LoggerFactory.getLogger(JMXDataCollectionConfigDao.class);

    public JMXDataCollectionConfigDao() {
        super(JmxDatacollectionConfig.class, "jmx-data-collection");
    }

    @Override
    protected JmxDatacollectionConfig translateConfig(JmxDatacollectionConfig config) {
        for (JmxCollection collection : config.getJmxCollectionList()) {
            if (collection.hasImportMbeans()) {
                for (String importMbeans : collection.getImportGroupsList()) {
                    final File file = new File(ConfigFileConstants.getHome(), "/etc/" + importMbeans);
                    LOG.debug("parseJmxMbeans: parsing {}", file);
                    final Mbeans mbeans = JaxbUtils.unmarshal(Mbeans.class, new FileSystemResource(file));
                    // TODO: What if there are some mbeans in the group ?
                    collection.addMbeans(mbeans.getMbeanList());
                }
            }
        }
        return config;
    }

    public JmxDatacollectionConfig getConfig() {
        return getContainer().getObject();
    }

}
