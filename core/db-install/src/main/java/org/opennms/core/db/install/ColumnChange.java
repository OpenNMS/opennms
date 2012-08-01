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

public class ColumnChange {
    private Column m_column = null;

    private ColumnChangeReplacement m_columnReplacement = null;

    private boolean m_upgradeTimestamp = false;

    private int m_selectIndex = 0;

    private int m_prepareIndex = 0;

    private int m_columnType = 0;

    /**
     * <p>getColumn</p>
     *
     * @return a {@link org.opennms.netmgt.dao.db.Column} object.
     */
    public Column getColumn() {
        return m_column;
    }

    /**
     * <p>setColumn</p>
     *
     * @param column a {@link org.opennms.netmgt.dao.db.Column} object.
     */
    public void setColumn(Column column) {
        m_column = column;
    }

    /**
     * <p>getColumnReplacement</p>
     *
     * @return a {@link org.opennms.netmgt.dao.db.ColumnChangeReplacement} object.
     */
    public ColumnChangeReplacement getColumnReplacement() {
        return m_columnReplacement;
    }

    /**
     * <p>hasColumnReplacement</p>
     *
     * @return a boolean.
     */
    public boolean hasColumnReplacement() {
        return (m_columnReplacement != null);
    }

    /**
     * <p>setColumnReplacement</p>
     *
     * @param columnReplacement a {@link org.opennms.netmgt.dao.db.ColumnChangeReplacement} object.
     */
    public void setColumnReplacement(ColumnChangeReplacement columnReplacement) {
        m_columnReplacement = columnReplacement;
    }

    /**
     * <p>isUpgradeTimestamp</p>
     *
     * @return a boolean.
     */
    public boolean isUpgradeTimestamp() {
        return m_upgradeTimestamp;
    }

    /**
     * <p>setUpgradeTimestamp</p>
     *
     * @param upgradeTimestamp a boolean.
     */
    public void setUpgradeTimestamp(boolean upgradeTimestamp) {
        m_upgradeTimestamp = upgradeTimestamp;
    }

    /**
     * <p>getSelectIndex</p>
     *
     * @return a int.
     */
    public int getSelectIndex() {
        return m_selectIndex;
    }

    /**
     * <p>setSelectIndex</p>
     *
     * @param selectIndex a int.
     */
    public void setSelectIndex(int selectIndex) {
        m_selectIndex = selectIndex;
    }

    /**
     * <p>getPrepareIndex</p>
     *
     * @return a int.
     */
    public int getPrepareIndex() {
        return m_prepareIndex;
    }

    /**
     * <p>setPrepareIndex</p>
     *
     * @param prepareIndex a int.
     */
    public void setPrepareIndex(int prepareIndex) {
        m_prepareIndex = prepareIndex;
    }

    /**
     * <p>getColumnType</p>
     *
     * @return a int.
     */
    public int getColumnType() {
        return m_columnType;
    }

    /**
     * <p>setColumnType</p>
     *
     * @param columnType a int.
     */
    public void setColumnType(int columnType) {
        m_columnType = columnType;
    }
}
