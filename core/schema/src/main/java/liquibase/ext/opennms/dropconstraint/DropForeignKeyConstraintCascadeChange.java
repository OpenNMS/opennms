package liquibase.ext.opennms.dropconstraint;

import liquibase.change.core.DropForeignKeyConstraintChange;
import liquibase.database.Database;
import liquibase.statement.SqlStatement;

public class DropForeignKeyConstraintCascadeChange extends DropForeignKeyConstraintChange {

	private String m_cascade = "false";

	public DropForeignKeyConstraintCascadeChange() {
		super();
		setPriority(getChangeMetaData().getPriority() + 1);
	}

	public String getCascade() {
		return m_cascade.toString();
	}
	
	public void setCascade(final String cascade) {
		m_cascade = cascade;
	}
	
    public SqlStatement[] generateStatements(final Database database) {
    	return DropForeignKeyConstraintCascadeStatement.createFromSqlStatements(super.generateStatements(database), Boolean.valueOf(m_cascade));
    }

}
