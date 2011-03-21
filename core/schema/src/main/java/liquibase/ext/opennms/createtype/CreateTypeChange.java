package liquibase.ext.opennms.createtype;

import java.util.ArrayList;
import java.util.List;

import liquibase.change.AbstractChange;
import liquibase.change.ChangeMetaData;
import liquibase.change.ChangeWithColumns;
import liquibase.change.ColumnConfig;
import liquibase.database.Database;
import liquibase.database.core.PostgresDatabase;
import liquibase.statement.SqlStatement;

public class CreateTypeChange extends AbstractChange implements ChangeWithColumns {

	private String m_typeName;
	private List<ColumnConfig> m_columns = new ArrayList<ColumnConfig>();

	public CreateTypeChange() {
		super("createType", "Create a new column type.", ChangeMetaData.PRIORITY_DEFAULT);
	}

    public boolean supports(final Database database) {
    	return database instanceof PostgresDatabase;
    }

	public String getName() {
		return m_typeName;
	}
	
	public void setName(final String name) {
		m_typeName = name;
	}

	public void addColumn(final ColumnConfig column) {
		m_columns.add(column);
	}

	public List<ColumnConfig> getColumns() {
		return m_columns;
	}

	public SqlStatement[] generateStatements(final Database database) {
		final CreateTypeStatement statement = new CreateTypeStatement(m_typeName);
		for (final ColumnConfig column : m_columns) {
			statement.addColumn(column.getName(), column.getType());
		}
		return new SqlStatement[] {
				statement
		};
	}

    public String getConfirmationMessage() {
        return "Type " + getName() + " created";
    }

}
