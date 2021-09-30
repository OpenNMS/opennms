/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.opennms.core.xml.AbstractJaxbConfigDao;
import org.opennms.netmgt.config.hardware.HwExtension;
import org.opennms.netmgt.config.hardware.HwInventoryAdapterConfiguration;
import org.opennms.netmgt.config.hardware.MibObj;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class DefaultSnmpHwInventoryAdapterConfigDao.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class DefaultSnmpHwInventoryAdapterConfigDao extends AbstractJaxbConfigDao<HwInventoryAdapterConfiguration, HwInventoryAdapterConfiguration>  implements SnmpHwInventoryAdapterConfigDao {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(DefaultSnmpHwInventoryAdapterConfigDao.class);

    /**
     * The Constructor.
     */
    public DefaultSnmpHwInventoryAdapterConfigDao() {
        super(HwInventoryAdapterConfiguration.class, "hardware-inventory-adapter-configuration");
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.config.HwInventoryAdapterConfigurationDao#getConfiguration()
     */
    @Override
    public HwInventoryAdapterConfiguration getConfiguration() {
        return getContainer().getObject();
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.config.HwInventoryAdapterConfigurationDao#reload()
     */
    @Override
    public void reload() {
        getContainer().reload();
    }

    /* (non-Javadoc)
     * @see org.opennms.core.xml.AbstractJaxbConfigDao#translateConfig(java.lang.Object)
     */
    @Override
    protected HwInventoryAdapterConfiguration translateConfig(HwInventoryAdapterConfiguration config) {
        final Set<String> oids = new HashSet<>();
        final Set<String> names = new HashSet<>();

        for (HwExtension ext : config.getExtensions()) {
            for (Iterator<MibObj> it = ext.getMibObjects().iterator(); it.hasNext();) {
                MibObj obj = it.next();
                final String oid = obj.getOid().toString();
                if (oids.contains(oid)) {
                    LOG.warn("Duplicate OID detected, ignoring {} (please fix the configuration file)", obj);
                    it.remove();
                    continue;
                } else {
                    oids.add(oid);
                }
                if (names.contains(obj.getAlias())) {
                    LOG.warn("Duplicate Alias detected, ignoring {} (please fix the configuration file)", obj);
                    it.remove();
                    continue;
                } else {
                    names.add(obj.getAlias());
                }
            }
        }

        return config;
    }

}
