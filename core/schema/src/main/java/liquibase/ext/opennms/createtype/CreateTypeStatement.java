package liquibase.ext.opennms.createtype;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import liquibase.statement.AbstractSqlStatement;

import org.apache.commons.lang.builder.ToStringBuilder;

public class CreateTypeStatement extends AbstractSqlStatement {

	private String m_name;
	private List<String> m_columns = new ArrayList<String>();
	private Map<String,String> m_columnTypes = new HashMap<String,String>();

	public CreateTypeStatement(final String name) {
		m_name = name;
	}

	public CreateTypeStatement addColumn(final String name, final String type) {
		m_columns.add(name);
		m_columnTypes.put(name, type);
		return this;
	}
	
	public String getName() {
		return m_name;
	}
	
	public List<String> getColumns() {
		return m_columns;
	}
	
	public String getColumnType(final String name) {
		return m_columnTypes.get(name);
	}
	
	public String toString() {
		return new ToStringBuilder(this)
			.append("name", m_name)
			.append("columns", m_columnTypes)
			.toString();
	}
}
