package liquibase.ext.opennms.autoincrement;

import java.util.ArrayList;
import java.util.List;

import liquibase.change.core.AddAutoIncrementChange;
import liquibase.database.Database;
import liquibase.database.core.PostgresDatabase;
import liquibase.statement.DatabaseFunction;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.AddDefaultValueStatement;
import liquibase.statement.core.CreateSequenceStatement;
import liquibase.statement.core.SetNullableStatement;

public class AddNamedAutoIncrementChange extends AddAutoIncrementChange {

    private String m_sequenceName;

	public AddNamedAutoIncrementChange() {
    	super();
    	setPriority(getChangeMetaData().getPriority() + 1);
    }

    public String getSequenceName() {
    	return m_sequenceName;
    }
    
    public void setSequenceName(final String sequenceName) {
    	m_sequenceName = sequenceName;
    }
    
    public SqlStatement[] generateStatements(final Database database) {
    	final List<SqlStatement> statements = new ArrayList<SqlStatement>();
        if (database instanceof PostgresDatabase) {
    		String sequenceName = m_sequenceName;
        	if (m_sequenceName == null) {
        		sequenceName = (getTableName() + "_" + getColumnName() + "_seq").toLowerCase();
        		statements.add(new CreateSequenceStatement(getSchemaName(), sequenceName));
        	}
        	statements.add(new SetNullableStatement(getSchemaName(), getTableName(), getColumnName(), null, false));
        	statements.add(new AddDefaultValueStatement(getSchemaName(), getTableName(), getColumnName(), getColumnDataType(), new DatabaseFunction("NEXTVAL('"+sequenceName+"')")));
        	return statements.toArray(new SqlStatement[0]);
        } else {
        	return super.generateStatements(database);
        }
    }

}
