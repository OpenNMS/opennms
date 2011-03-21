package liquibase.ext.opennms.dropconstraint;

import java.util.ArrayList;
import java.util.List;

import liquibase.statement.SqlStatement;
import liquibase.statement.core.DropForeignKeyConstraintStatement;

public class DropForeignKeyConstraintCascadeStatement extends DropForeignKeyConstraintStatement {

	private Boolean m_cascade;

	public DropForeignKeyConstraintCascadeStatement(final DropForeignKeyConstraintStatement statement, Boolean cascade) {
		super(statement.getBaseTableSchemaName(), statement.getBaseTableName(), statement.getConstraintName());
		m_cascade = cascade;
	}

	public DropForeignKeyConstraintCascadeStatement setCascade(final Boolean cascade) {
		m_cascade = cascade;
		return this;
	}
	
	public Boolean getCascade() {
		return m_cascade;
	}

	public static SqlStatement[] createFromSqlStatements(final SqlStatement[] superSql, final Boolean cascade) {
		final List<SqlStatement> statements = new ArrayList<SqlStatement>();
		
		for (final SqlStatement statement : superSql) {
			if (statement instanceof DropForeignKeyConstraintStatement) {
				statements.add(new DropForeignKeyConstraintCascadeStatement((DropForeignKeyConstraintStatement)statement, cascade));
			} else {
				statements.add(statement);
			}
		}
		return statements.toArray(new SqlStatement[0]);
	}
}
