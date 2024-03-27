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
