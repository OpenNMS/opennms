/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.jaxb;

import org.opennms.core.xml.AbstractJaxbConfigDao;
import org.opennms.netmgt.config.jdbc.JdbcDataCollection;
import org.opennms.netmgt.config.jdbc.JdbcDataCollectionConfig;
import org.opennms.netmgt.dao.JdbcDataCollectionConfigDao;

public class JdbcDataCollectionConfigDaoJaxb extends AbstractJaxbConfigDao<JdbcDataCollectionConfig, JdbcDataCollectionConfig> implements JdbcDataCollectionConfigDao {

    public JdbcDataCollectionConfigDaoJaxb() {
        super(JdbcDataCollectionConfig.class, "JDBC Data Collection Configuration");
    }

    @Override
    public JdbcDataCollection getDataCollectionByName(String name) {
        JdbcDataCollectionConfig jdcc = getContainer().getObject();
        for (JdbcDataCollection dataCol : jdcc.getJdbcDataCollections()) {
            if(dataCol.getName().equals(name)) {
                return dataCol;
            }
        }

        return null;
    }

    @Override
    public JdbcDataCollection getDataCollectionByIndex(int idx) {
        JdbcDataCollectionConfig jdcc = getContainer().getObject();
        return jdcc.getJdbcDataCollections().get(idx);
    }

    @Override
    public JdbcDataCollectionConfig getConfig() {
        return getContainer().getObject();
    }

    @Override
    protected JdbcDataCollectionConfig translateConfig(JdbcDataCollectionConfig jaxbConfig) {
        return jaxbConfig;
    }

}
