package org.opennms.install;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Constraint {
    public static final int PRIMARY_KEY = 1;
    public static final int FOREIGN_KEY = 2;

    private String m_name;
    private int m_type;
    private String m_column;
    private String m_ftable;
    private String m_fcolumn;

    public Constraint(String constraint) throws Exception {
	this.parse(constraint);
    }

    public Constraint(String name, String column) {
	m_name = name;
	m_type = PRIMARY_KEY;
	m_column = column;
    }

    public Constraint(String name, String column, String ftable,
		      String fcolumn) {
	m_name = name;
	m_type = FOREIGN_KEY;
	m_column = column;
	m_ftable = ftable;
	m_fcolumn = fcolumn;
    }

    public String getName() {
	return m_name;
    }

    public void setName(String name) {
	m_name = name.toLowerCase();
    }

    public int getType() {
	return m_type;
    }

    public void setType(int type) {
	m_type = type;
    }

    public String getColumn() {
	return m_column;
    }

    public void setColumn(String column) {
	m_column = column.toLowerCase();
    }

    public String getForeignTable() {
	return m_ftable;
    }

    public void setForeignTable(String ftable) {
	m_ftable = ftable.toLowerCase();
    }

    public String getForeignColumn() {
	return m_fcolumn;
    }

    public void setForeignColumn(String fcolumn) {
	m_fcolumn = fcolumn.toLowerCase();
    }

    public String toString() {
	StringBuffer b = new StringBuffer();

	b.append("constraint ");
	b.append(m_name);

	switch(m_type) {
	case PRIMARY_KEY:
	    b.append(" primary key (");
	    break;

	case FOREIGN_KEY:
	    b.append(" foreign key (");
	    break;
	}

	b.append(m_column);
	b.append(")");

	if (m_type == FOREIGN_KEY) {
	    b.append(" references ");
	    b.append(m_ftable);
	    b.append(" (");
	    b.append(m_fcolumn);
	    b.append(")");
	}

	return b.toString();
    }

    public void parse(String constraint) throws Exception {
	Matcher m;

	m = Pattern.compile("(?i)constraint (\\S+) " +
			    "primary key \\((\\S+)\\)").
	    matcher(constraint);
	if (m.matches()) {
	    setName(m.group(1));
	    setType(PRIMARY_KEY);
	    setColumn(m.group(2));
	    return;
	}
	    
	m = Pattern.compile("(?i)constraint (\\S+) " +
			    "foreign key \\((\\S+)\\) " +
			    "references (\\S+)" +
			    "(?: \\((\\S+)\\))?").matcher(constraint);
	if (m.matches()) {
	    setName(m.group(1));
	    setType(FOREIGN_KEY);
	    setColumn(m.group(2));
	    setForeignTable(m.group(3));
	    if (m.group(4) == null) {
		setForeignColumn(m.group(2));
	    } else {
		setForeignColumn(m.group(4));
	    }
	    return;
	}

	throw new Exception("Cannot parse constraint: " + constraint);
    }

    public boolean equals(Object other_o) {
	Constraint other = (Constraint) other_o;
	    
	if ((m_name == null && other.getName() != null) ||
	    (m_name != null && other.getName() == null)) {
	    return false;
	}
	if (m_name != null && other.getName() != null &&
	    !m_name.equals(other.getName())) {
	    return false;
	}

	if (m_type != other.getType()) {
	    return false;
	}

	if ((m_column == null && other.getColumn() != null) ||
	    (m_column != null && other.getColumn() == null)) {
	    return false;
	}
	if (m_column != null && other.getColumn() != null &&
	    !m_column.equals(other.getColumn())) {
	    return false;
	}

	if ((m_ftable == null && other.getForeignTable() != null) ||
	    (m_ftable != null && other.getForeignTable() == null)) {
	    return false;
	}
	if (m_ftable != null && other.getForeignTable() != null &&
	    !m_ftable.equals(other.getForeignTable())) {
	    return false;
	}

	if ((m_fcolumn == null && other.getForeignColumn() != null) ||
	    (m_fcolumn != null && other.getForeignColumn() == null)) {
	    return false;
	}
	if (m_fcolumn != null && other.getForeignColumn() != null &&
	    !m_fcolumn.equals(other.getForeignColumn())) {
	    return false;
	}

	return true;
    }

    public int hashCode() {
	return m_name.hashCode() +
	    new Integer(m_type).hashCode() +
	    m_column.hashCode() +
	    m_ftable.hashCode() +
	    m_fcolumn.hashCode();
    }
}
