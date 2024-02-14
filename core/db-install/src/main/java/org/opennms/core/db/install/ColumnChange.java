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
