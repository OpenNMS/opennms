/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
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
        return getContainer().getObject();
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
