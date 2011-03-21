package liquibase.ext.opennms.createindex;

import liquibase.statement.SqlStatement;
import liquibase.statement.core.CreateIndexStatement;

public class CreateIndexWithWhereStatement extends CreateIndexStatement
		implements SqlStatement {

	private String m_where;

	public CreateIndexWithWhereStatement(final String indexName, final String tableSchemaName, final String tableName, final Boolean isUnique, final String associatedWith, final String... columns) {
		super(indexName, tableSchemaName, tableName, isUnique, associatedWith, columns);
	}

	public CreateIndexWithWhereStatement(final CreateIndexStatement statement, final String where) {
		this(statement.getIndexName(), statement.getTableSchemaName(), statement.getTableName(), statement.isUnique(), statement.getAssociatedWith(), statement.getColumns());
		m_where = where;
	}

	public String getWhere() {
		return m_where;
	}
	
	public CreateIndexWithWhereStatement setWhere(final String where) {
		m_where = where;
		return this;
	}
}
