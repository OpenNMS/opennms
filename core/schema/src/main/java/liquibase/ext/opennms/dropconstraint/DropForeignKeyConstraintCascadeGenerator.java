package liquibase.ext.opennms.dropconstraint;

import liquibase.database.Database;
import liquibase.database.core.PostgresDatabase;
import liquibase.database.structure.DatabaseObject;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.core.DropForeignKeyConstraintGenerator;
import liquibase.statement.core.DropForeignKeyConstraintStatement;

public class DropForeignKeyConstraintCascadeGenerator extends DropForeignKeyConstraintGenerator {

	@Override
	public int getPriority() {
		return super.getPriority() + 1;
	}

    public Sql[] generateSql(final DropForeignKeyConstraintStatement statement, final Database database, final SqlGeneratorChain sqlGeneratorChain) {
		final Sql[] superSql = super.generateSql(statement, database, sqlGeneratorChain);
		if (statement instanceof DropForeignKeyConstraintCascadeStatement) {
			Boolean cascade = ((DropForeignKeyConstraintCascadeStatement)statement).getCascade();
			if (cascade != null && cascade && database instanceof PostgresDatabase) {
	    		return new Sql[] {
	    				new UnparsedSql(superSql[0].toSql() + " CASCADE", superSql[0].getEndDelimiter(), superSql[0].getAffectedDatabaseObjects().toArray(new DatabaseObject[0]))
	    		};
	    	} else {
	    		return superSql;
	    	}
		} else {
			return superSql;
		}
    }

}
