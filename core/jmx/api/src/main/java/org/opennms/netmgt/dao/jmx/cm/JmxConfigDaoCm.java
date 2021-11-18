/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.jmx.cm;

import org.opennms.features.config.service.impl.AbstractCmJaxbConfigDao;
import org.opennms.netmgt.config.jmx.JmxConfig;
import org.opennms.netmgt.dao.jmx.JmxConfigDao;

/**
 * Implementation for JMX config DAO class using configuration manager.
 *
 * @author Dmitri Herdt <dmitri@herdt.online>
 */
public class JmxConfigDaoCm extends AbstractCmJaxbConfigDao<JmxConfig> implements JmxConfigDao {
    
    private static final String CONFIG_NAME = "jmx";
    private static final String DEFAULT_CONFIG_ID = "default";

    public JmxConfigDaoCm() {
        super(JmxConfig.class, "JMX Configuration");
    }


    @Override
    public JmxConfig getConfig() {
        return this.getConfig(this.getDefaultConfigId());
    }

    @Override
    protected String getConfigName() {
        return CONFIG_NAME;
    }

    @Override
    protected String getDefaultConfigId() {
        return DEFAULT_CONFIG_ID;
    }
}