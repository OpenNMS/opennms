package org.opennms.install;

public class ColumnChange {
    private Column m_column = null;
    private Object m_nullReplace = null;
    private boolean m_upgradeTimestamp = false;
    private int m_selectIndex = 0;
    private int m_prepareIndex = 0;
    private int m_columnType = 0;

    public Column getColumn() {
	return m_column;
    }

    public void setColumn(Column column) {
	m_column = column;
    }

    public Object getNullReplace() {
	return m_nullReplace;
    }

    public boolean isNullReplace() {
	return (m_nullReplace != null);
    }

    public void setNullReplace(Object nullReplace) {
	m_nullReplace = nullReplace;
    }

    public boolean isUpgradeTimestamp() {
	return m_upgradeTimestamp;
    }

    public void setUpgradeTimestamp(boolean upgradeTimestamp) {
	m_upgradeTimestamp = upgradeTimestamp;
    }

    public int getSelectIndex() {
	return m_selectIndex;
    }

    public void setSelectIndex(int selectIndex) {
	m_selectIndex = selectIndex;
    }

    public int getPrepareIndex() {
	return m_prepareIndex;
    }

    public void setPrepareIndex(int prepareIndex) {
	m_prepareIndex = prepareIndex;
    }

    public int getColumnType() {
	return m_columnType;
    }

    public void setColumnType(int columnType) {
	m_columnType = columnType;
    }
}
