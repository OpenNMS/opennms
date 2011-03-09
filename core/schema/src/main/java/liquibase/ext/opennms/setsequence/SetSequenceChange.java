package liquibase.ext.opennms.setsequence;

import java.util.ArrayList;
import java.util.List;

import liquibase.change.AbstractChange;
import liquibase.change.ChangeMetaData;
import liquibase.database.Database;
import liquibase.statement.SqlStatement;

public class SetSequenceChange extends AbstractChange {
    private String m_sequenceName;
	private Integer m_value;
	private List<TableConfig> m_tables = new ArrayList<TableConfig>();

	public SetSequenceChange() {
        super("setSequence", "Set Sequence", ChangeMetaData.PRIORITY_DEFAULT);
    }
    
	public void setSequenceName(final String sequenceName) {
		m_sequenceName = sequenceName;
	}
	
	public String getSequenceName() {
		return m_sequenceName;
	}

	public void setValue(final String value) {
		m_value = Integer.valueOf(value);
	}

	public String getValue() {
		return m_value.toString();
	}

	public TableConfig createTable() {
		final TableConfig tc = new TableConfig();
		m_tables.add(tc);
		return tc;
	}

	public void addTable(final TableConfig table) {
		m_tables.add(table);
	}

	public List<TableConfig> getTables() {
		return m_tables;
	}

	public SqlStatement[] generateStatements(final Database database) {
		final SetSequenceStatement statement = new SetSequenceStatement(getSequenceName());
		statement.setValue(m_value);
		for (final TableConfig table : m_tables) {
			statement.addTable(table.getName(), table.getSchemaName(), table.getColumn());
		}
		return new SqlStatement[] { statement };
	}

	public String getConfirmationMessage() {
		return "Sequence " + m_sequenceName + " updated";
	}

}
