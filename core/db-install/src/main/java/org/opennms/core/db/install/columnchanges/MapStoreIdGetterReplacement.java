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

/**
 * <p>MapStoreIdGetterReplacement class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
package org.opennms.core.db.install.columnchanges;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.opennms.core.db.install.ColumnChange;
import org.opennms.core.db.install.ColumnChangeReplacement;

public class MapStoreIdGetterReplacement implements ColumnChangeReplacement {
    private final AutoIntegerIdMapStoreReplacement m_storeFoo;
    private final String[] m_indexColumns;
    private final boolean m_noMatchOkay;
    
    /**
     * <p>Constructor for MapStoreIdGetterReplacement.</p>
     *
     * @param storeFoo a {@link org.opennms.netmgt.dao.db.columnchanges.AutoIntegerIdMapStoreReplacement} object.
     * @param columns an array of {@link java.lang.String} objects.
     * @param noMatchOkay a boolean.
     */
    public MapStoreIdGetterReplacement(AutoIntegerIdMapStoreReplacement storeFoo,
            String[] columns, boolean noMatchOkay) {
        m_storeFoo = storeFoo;
        m_indexColumns = columns;
        m_noMatchOkay = noMatchOkay;
    }

    /** {@inheritDoc} */
    @Override
    public Object getColumnReplacement(ResultSet rs, Map<String, ColumnChange> columnChanges) throws SQLException {
        return m_storeFoo.getIntegerForColumns(rs, columnChanges, m_indexColumns, m_noMatchOkay);
    }
    
    /**
     * <p>addColumnIfColumnIsNew</p>
     *
     * @return a boolean.
     */
    @Override
    public boolean addColumnIfColumnIsNew() {
        return true;
    }
    
    /**
     * <p>close</p>
     */
    @Override
    public void close() {
    }
}
