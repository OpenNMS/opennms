package liquibase.ext.opennms.setsequence;

public class TableConfig {

	private String m_name;
	private String m_schemaName;
	private String m_column;

	public TableConfig() {
	}

	public TableConfig(final String name, final String schemaName, final String column) {
		m_name = name;
		m_schemaName = schemaName;
		m_column = column;
	}

	public void setName(final String name) {
		m_name = name;
	}

	public String getName() {
		return m_name;
	}

	public void setSchemaName(final String schemaName) {
		m_schemaName = schemaName;
	}

	public String getSchemaName() {
		return m_schemaName;
	}

	public void setColumn(final String column) {
		m_column = column;
	}

	public String getColumn() {
		return m_column;
	}
}
