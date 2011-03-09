package liquibase.ext.opennms.setsequence;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;

import liquibase.statement.SqlStatement;

public class SetSequenceStatement implements SqlStatement {
	private final String m_sequenceName;
	private final List<String> m_tables = new ArrayList<String>();
	private Map<String, String> m_columns = new LinkedHashMap<String, String>();
	private Map<String, String> m_schemas = new LinkedHashMap<String, String>();
	private Integer m_value;

	public SetSequenceStatement(final String sequenceName) {
		m_sequenceName = sequenceName;
	}

	public boolean skipOnUnsupported() {
		return true;
	}

	public String getSequenceName() {
		return m_sequenceName;
	}

	public List<String> getTables() {
		return m_tables;
	}

	public Map<String,String> getColumns() {
		return m_columns;
	}

	public Map<String,String> getSchemas() {
		return m_schemas;
	}
	
	public Integer getValue() {
		return m_value;
	}

	public SetSequenceStatement setValue(final Integer value) {
		m_value = value;
		return this;
	}
	
	SetSequenceStatement addTable(final String name, final String column) {
		getTables().add(name);
		getColumns().put(name, column);
		return this;
	}

	SetSequenceStatement addTable(final String name, final String schemaName, final String column) {
		getTables().add(name);
		getColumns().put(name, column);
		getSchemas().put(name, schemaName);
		return this;
	}
	
	public String toString() {
		return new ToStringBuilder(this)
			.append("sequenceName", m_sequenceName)
			.append("value", m_value)
			.append("tables", m_tables)
			.append("columns", m_columns)
			.append("schemas", m_schemas)
			.toString();
	}
}
