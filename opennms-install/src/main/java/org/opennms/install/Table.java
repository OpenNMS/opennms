package org.opennms.install;

import java.util.List;

public class Table {
	private String m_name;
	private List<Column> m_columns;
	private List<Constraint> m_constraints;
	
	public List<Column> getColumns() {
		return m_columns;
	}
	public void setColumns(List<Column> columns) {
		m_columns = columns;
	}
	
	public List<Constraint> getConstraints() {
		return m_constraints;
	}
	public void setConstraints(List<Constraint> constraints) {
		m_constraints = constraints;
	}
	
	public String getName() {
		return m_name;
	}
	public void setName(String name) {
		m_name = name.toLowerCase();
	}
	
	public void setNotNullOnPrimaryKeyColumns() throws Exception {
		for (Constraint constraint : getConstraints()) {
			if (constraint.isPrimaryKeyConstraint()) {
				for (String constrainedColumn : constraint.getColumns()) {
					boolean foundColumn = false;
					for (Column column : getColumns()) {
						if (constrainedColumn.equals(column.getName())) {
							column.setNotNull(true);
							foundColumn = true;
							break;
						}
					}
					if (!foundColumn) {
						throw new Exception("could not find column '" + constrainedColumn + "' for constraint: " + constraint);
					}
				}
			}
		}
	}
	
	public boolean equals(Object o) {
		if (o == null || !(o instanceof Table)) {
			return false;
			
		}
		Table other = (Table) o;

		if ((m_name == null && other.getName() != null) || (m_name != null && other.getName() == null)) {
            return false;
        }
        if (m_name != null && other.getName() != null && !m_name.equals(other.getName())) {
            return false;
        }

        if ((m_columns == null && other.getColumns() != null) || (m_columns != null && other.getColumns() == null)) {
            return false;
        }
        if (m_columns != null && other.getColumns() != null && !m_columns.equals(other.getColumns())) {
            return false;
        }
        
		if ((m_constraints == null && other.getConstraints() != null) || (m_constraints != null && other.getConstraints() == null)) {
            return false;
        }
        if (m_constraints != null && other.getConstraints() != null && !m_constraints.equals(other.getConstraints())) {
            return false;
        }

		return true;
	}
	
}
