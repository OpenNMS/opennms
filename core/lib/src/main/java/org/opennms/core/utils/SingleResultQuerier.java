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

package org.opennms.core.utils;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;


/**
 * <p>SingleResultQuerier class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class SingleResultQuerier extends Querier {
    /**
     * <p>Constructor for SingleResultQuerier.</p>
     *
     * @param db a {@link javax.sql.DataSource} object.
     * @param sql a {@link java.lang.String} object.
     */
    public SingleResultQuerier(DataSource db, String sql) {
        super(db, sql);
    }
    
    private Object m_result;
    
    /**
     * <p>getResult</p>
     *
     * @return a {@link java.lang.Object} object.
     */
    public Object getResult() { return m_result; }
    
    /** {@inheritDoc} */
    @Override
    public void processRow(ResultSet rs) throws SQLException {
        m_result = rs.getObject(1);
    }
    
}
