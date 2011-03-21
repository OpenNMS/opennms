package liquibase.ext.opennms.createtype;

import java.util.List;

import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.core.AbstractSqlGenerator;

public class CreateTypeGenerator extends AbstractSqlGenerator<CreateTypeStatement> {

	public ValidationErrors validate(final CreateTypeStatement statement, final Database database, final SqlGeneratorChain sqlGeneratorChain) {
		ValidationErrors errors = new ValidationErrors();
		errors.checkRequiredField("name", statement);
		errors.checkRequiredField("columns", statement);
		return errors;
	}

	// example: CREATE TYPE daily_series AS (ds timestamp without time zone, de timestamp without time zone, dow integer);
    public Sql[] generateSql(final CreateTypeStatement statement, final Database database, final SqlGeneratorChain sqlGeneratorChain) {
    	final StringBuffer sb = new StringBuffer();

    	sb.append("CREATE TYPE " + database.escapeColumnName(null, null, statement.getName()))
    			.append(" AS (");
    	final List<String> columns = statement.getColumns();
		for (int i = 0; i < columns.size(); i++) {
    		final String columnName = columns.get(i);
    		final String columnType = statement.getColumnType(columnName);
    		
    		sb.append(database.escapeColumnName(null, null, columnName));
    		sb.append(" ");
    		sb.append(columnType);
    		if (i < columns.size() - 1) {
    			sb.append(", ");
    		}
    	}
		sb.append(")");
		return new Sql[] {
				new UnparsedSql(sb.toString())
		};
    }
}
