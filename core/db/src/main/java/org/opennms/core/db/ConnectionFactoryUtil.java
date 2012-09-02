/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

package org.opennms.core.db;

import java.io.InputStream;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.xml.CastorUtils;
import org.opennms.netmgt.config.opennmsDataSources.DataSourceConfiguration;
import org.opennms.netmgt.config.opennmsDataSources.JdbcDataSource;

public class ConnectionFactoryUtil {

    /**
     * <p>marshalDataSourceFromConfig</p>
     *
     * @param stream a {@link java.io.InputStream} object.
     * @param dsName a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.config.opennmsDataSources.JdbcDataSource} object.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public static JdbcDataSource marshalDataSourceFromConfig(final InputStream stream, final String dsName) throws MarshalException, ValidationException {
    	final DataSourceConfiguration dsc = CastorUtils.unmarshal(DataSourceConfiguration.class, stream);
        return validateDataSourceConfiguration(dsName, dsc);
    }

    private static JdbcDataSource validateDataSourceConfiguration(final String dsName, final DataSourceConfiguration dsc) {
        for (final JdbcDataSource jdbcDs : dsc.getJdbcDataSourceCollection()) {
            if (jdbcDs.getName().equals(dsName)) {
                return jdbcDs;
            }
        }
        
        throw new IllegalArgumentException("ConnectionFactoryUtil: DataSource: "+dsName+" is not defined.");
    }

}
