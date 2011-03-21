package liquibase.ext.opennms.createindex;

import liquibase.database.Database;
import liquibase.database.structure.DatabaseObject;
import liquibase.logging.LogFactory;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.core.CreateIndexGenerator;
import liquibase.statement.core.CreateIndexStatement;

public class CreateIndexWithWhereGenerator extends CreateIndexGenerator {

	public int getPriority() {
		return super.getPriority() + 1;
	}

    public Sql[] generateSql(final CreateIndexStatement statement, final Database database, final SqlGeneratorChain sqlGeneratorChain) {
    	final Sql[] superSql = super.generateSql(statement, database, sqlGeneratorChain);

    	if (statement instanceof CreateIndexWithWhereStatement) {
    		if (superSql.length != 1) {
    			LogFactory.getLogger().warning("expected 1 create index statement, but got " + superSql.length);
            	return superSql;
    		}
    		
    		return new Sql[] {
    				new UnparsedSql(superSql[0].toSql() + " WHERE " + ((CreateIndexWithWhereStatement)statement).getWhere(),
    						superSql[0].getEndDelimiter(), superSql[0].getAffectedDatabaseObjects().toArray(new DatabaseObject[0]))
    		};
    	} else {
    		return superSql;
    	}
    }

}
