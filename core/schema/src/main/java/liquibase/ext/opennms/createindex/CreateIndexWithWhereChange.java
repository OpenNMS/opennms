package liquibase.ext.opennms.createindex;

import liquibase.database.Database;
import liquibase.logging.LogFactory;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.CreateIndexStatement;

public class CreateIndexWithWhereChange extends liquibase.change.core.CreateIndexChange {

	private String m_where;

	public CreateIndexWithWhereChange() {
		super();
		setPriority(getChangeMetaData().getPriority() + 1);
	}

	public String getWhere() {
		return m_where;
	}
	
	public void setWhere(final String where) {
		m_where = where;
	}
	
	@Override
	public SqlStatement[] generateStatements(final Database database) {
		final SqlStatement[] superStatements = super.generateStatements(database);
		if (m_where == null) return superStatements;
		
		if (superStatements.length != 1) {
			LogFactory.getLogger().warning("expected 1 create index statement, but got " + superStatements.length);
			return superStatements;
		}
		
	    return new SqlStatement[]{
	    		new CreateIndexWithWhereStatement((CreateIndexStatement)superStatements[0], m_where)
	    };
    }

}
