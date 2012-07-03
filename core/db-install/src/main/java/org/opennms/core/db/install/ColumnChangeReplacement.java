/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
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

package org.opennms.core.db.install;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public interface ColumnChangeReplacement {
    /**
     * <p>getColumnReplacement</p>
     *
     * @param rs a {@link java.sql.ResultSet} object.
     * @param columnChanges a {@link java.util.Map} object.
     * @return a {@link java.lang.Object} object.
     * @throws java.sql.SQLException if any.
     */
    public Object getColumnReplacement(ResultSet rs, Map<String, ColumnChange> columnChanges) throws SQLException;
    /**
     * <p>addColumnIfColumnIsNew</p>
     *
     * @return a boolean.
     */
    public boolean addColumnIfColumnIsNew();
    /**
     * <p>close</p>
     *
     * @throws java.sql.SQLException if any.
     */
    public void close() throws SQLException;
}
