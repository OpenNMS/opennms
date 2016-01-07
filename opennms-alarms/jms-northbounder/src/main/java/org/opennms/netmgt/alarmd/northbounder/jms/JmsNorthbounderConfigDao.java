/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.alarmd.northbounder.jms;

import org.opennms.core.xml.AbstractJaxbConfigDao;

/**
 * The Class JmsNorthbounderConfigDao.
 *
 * @author David Schlenk <dschlenk@converge-one.com>
 */
public class JmsNorthbounderConfigDao extends
        AbstractJaxbConfigDao<JmsNorthbounderConfig, JmsNorthbounderConfig> {

    /**
     * Instantiates a new jms northbounder config dao.
     */
    public JmsNorthbounderConfigDao() {
        super(JmsNorthbounderConfig.class, "Config for JMS Northbounder");
    }

    /* (non-Javadoc)
     * @see org.opennms.core.xml.AbstractJaxbConfigDao#translateConfig(java.lang.Object)
     */
    @Override
    protected JmsNorthbounderConfig translateConfig(
            JmsNorthbounderConfig config) {
        return config;
    }

    /**
     * Gets the config.
     *
     * @return the config
     */
    public JmsNorthbounderConfig getConfig() {
        return getContainer().getObject();
    }

}
