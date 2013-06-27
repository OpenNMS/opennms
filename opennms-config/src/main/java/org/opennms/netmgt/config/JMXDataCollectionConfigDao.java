/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config;

import java.io.File;

import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.xml.AbstractJaxbConfigDao;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.collectd.jmx.JmxCollection;
import org.opennms.netmgt.config.collectd.jmx.JmxDatacollectionConfig;
import org.opennms.netmgt.config.collectd.jmx.Mbeans;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;

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
        for (JmxCollection collection : config.getJmxCollection()) {
            if (collection.getMbeans() == null) {
                collection.setMbeans(new Mbeans());
            }
            if (collection.hasImportMbeans()) {
                for (String importMbeans : collection.getImportGroupsList()) {
                    File file = new File(ConfigFileConstants.getHome(), "/etc/" + importMbeans);
                    LOG.debug("parseJmxMbeans: parsing {}", file);
                    Mbeans mbeans = JaxbUtils.unmarshal(Mbeans.class, new FileSystemResource(file));
                    // TODO: What if there are some mbeans in the group ?
                    collection.getMbeans().getMbeanCollection().addAll(mbeans.getMbeanCollection());
                }
            }
        }
        return config;
    }

    public JmxDatacollectionConfig getConfig() {
        return getContainer().getObject();
    }

}
