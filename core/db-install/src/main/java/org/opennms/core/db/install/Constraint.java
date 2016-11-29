/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2004-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.core.db.install;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.util.StringUtils;

public class Constraint {
    /** Constant <code>PRIMARY_KEY=1</code> */
    public static final int PRIMARY_KEY = 1;

    /** Constant <code>FOREIGN_KEY=2</code> */
    public static final int FOREIGN_KEY = 2;
    
    /** Constant <code>CHECK=3</code> */
    public static final int CHECK = 3;

    private String m_name;

    private int m_type;
    
    private String m_table;

    private List<String> m_columns;

    private String m_ftable;

    private List<String> m_fcolumns;

    private String m_fdeltype;

    private String m_fupdtype;
    
    private String m_checkExpression;

    /**
     * <p>Constructor for Constraint.</p>
     *
     * @param table a {@link java.lang.String} object.
     * @param constraint a {@link java.lang.String} object.
     * @throws java.lang.Exception if any.
     */
    public Constraint(String table, String constraint) throws Exception {
        this.parse(constraint);
        this.setTable(table);
    }

    /**
     * Construct a primary key constraint from it's required elements
     *
     * @param table a {@link java.lang.String} object.
     * @param name a {@link java.lang.String} object.
     * @param columns a {@link java.util.List} object.
     */
    public Constraint(String table, String name, List<String> columns) {
        setTable(table);
        setName(name);
        setType(PRIMARY_KEY);
        setColumns(columns);
    }
    
    /**
     * Construct a foreign key constraint from it's required elements
     *
     * @param table a {@link java.lang.String} object.
     * @param name a {@link java.lang.String} object.
     * @param columns a {@link java.util.List} object.
     * @param ftable a {@link java.lang.String} object.
     * @param fcolumns a {@link java.util.List} object.
     * @param fupdtype a {@link java.lang.String} object.
     * @param fdeltype a {@link java.lang.String} object.
     * @throws java.lang.Exception if any.
     */
    public Constraint(String table, String name, List<String> columns, String ftable, List<String> fcolumns, String fupdtype, String fdeltype) throws Exception {
        setTable(table);
        setName(name);
        setType(FOREIGN_KEY);
        setColumns(columns);
        setForeignTable(ftable);
        setForeignColumns(fcolumns);
        setForeignUpdType(fupdtype);
        setForeignDelType(fdeltype);
    }
    
    /**
     * Construct a check type constraint from it's required elements
     *
     * @param table a {@link java.lang.String} object.
     * @param name a {@link java.lang.String} object.
     * @param checkExpression a {@link java.lang.String} object.
     */
    public Constraint(String table, String name, String checkExpression) {
    	setTable(table);
    	setName(name);
    	setCheckExpression(checkExpression);
    	setType(CHECK);
    }

    /**
     * <p>setForeignUpdType</p>
     *
     * @param fupdtype a {@link java.lang.String} object.
     */
    public final void setForeignUpdType(String fupdtype) {
        m_fupdtype = fupdtype;
    }
    
    /**
     * <p>getForeignUpdType</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public final String getForeignUpdType() {
        return m_fupdtype;
    }

    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public final String getName() {
        return m_name;
    }

    /**
     * <p>setName</p>
     *
     * @param name a {@link java.lang.String} object.
     */
    public final void setName(String name) {
        m_name = name.toLowerCase();
    }

    /**
     * <p>getType</p>
     *
     * @return a int.
     */
    public final int getType() {
        return m_type;
    }

    /**
     * <p>setType</p>
     *
     * @param type a int.
     */
    public final void setType(int type) {
        m_type = type;
    }
    
    /**
     * <p>isPrimaryKeyConstraint</p>
     *
     * @return a boolean.
     */
    public boolean isPrimaryKeyConstraint() {
    	return m_type == PRIMARY_KEY;
    }
    
    /**
     * <p>isForeignKeyConstraint</p>
     *
     * @return a boolean.
     */
    public boolean isForeignKeyConstraint() {
    	return m_type == FOREIGN_KEY;
    }
    
    /**
     * <p>isCheckConstraint</p>
     *
     * @return a boolean.
     */
    public boolean isCheckConstraint() {
    	return m_type == CHECK;
    }
    
	/**
	 * <p>getTable</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public final String getTable() {
		return m_table;
	}

	/**
	 * <p>setTable</p>
	 *
	 * @param table a {@link java.lang.String} object.
	 */
	public final void setTable(String table) {
		m_table = table;
	}
    
    /**
     * <p>setColumns</p>
     *
     * @param columns a {@link java.util.List} object.
     */
    public final void setColumns(List<String> columns) {
    	m_columns = new ArrayList<String>(columns.size());
    	for (String i : columns) {
    		m_columns.add(i.toLowerCase());
    	}
    }

    /**
     * <p>getColumns</p>
     *
     * @return a {@link java.util.List} object.
     */
    public final List<String> getColumns() {
        return m_columns;
    }

    /**
     * <p>setColumn</p>
     *
     * @param column a {@link java.lang.String} object.
     */
    public final void setColumn(String column) {
    	List<String> columns = new ArrayList<String>(1);
        columns.add(column.toLowerCase());
        setColumns(columns);
    }

    /**
     * <p>getForeignTable</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public final String getForeignTable() {
        return m_ftable;
    }

    /**
     * <p>setForeignTable</p>
     *
     * @param ftable a {@link java.lang.String} object.
     */
    public final void setForeignTable(String ftable) {
        m_ftable = ftable.toLowerCase();
    }

    /**
     * <p>getForeignColumns</p>
     *
     * @return a {@link java.util.List} object.
     */
    public final List<String> getForeignColumns() {
        return m_fcolumns;
    }

    /**
     * <p>setForeignColumn</p>
     *
     * @param fcolumn a {@link java.lang.String} object.
     */
    public final void setForeignColumn(String fcolumn) {
    	List<String> fcolumns = new ArrayList<String>(1);
    	fcolumns.add(fcolumn.toLowerCase());
        setForeignColumns(fcolumns);
    }

    /**
     * <p>setForeignColumns</p>
     *
     * @param fcolumns a {@link java.util.List} object.
     */
    public final void setForeignColumns(List<String> fcolumns) {
    	List<String> newFcolumns = new ArrayList<String>(fcolumns.size());
    	for (String fcolumn : fcolumns) {
    		newFcolumns.add(fcolumn.toLowerCase());
    	}
        m_fcolumns = newFcolumns;
    }

    /**
     * <p>getForeignDelType</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public final String getForeignDelType() {
        return m_fdeltype;
    }

    /**
     * <p>setForeignDelType</p>
     *
     * @param fdeltype a {@link java.lang.String} object.
     * @throws java.lang.Exception if any.
     */
    public final void setForeignDelType(String fdeltype) throws Exception {
        if (fdeltype.equals("a") || fdeltype.equals("c") || fdeltype.equals("r") || fdeltype.equals("n") || fdeltype.equals("d")) {
            m_fdeltype = fdeltype;
        } else {
            throw new Exception("confdeltype \"" + fdeltype + "\" unknown");
        }
    }

	/**
	 * <p>getCheckExpression</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public final String getCheckExpression() {
		return m_checkExpression;
	}

	/**
	 * <p>setCheckExpression</p>
	 *
	 * @param expression a {@link java.lang.String} object.
	 */
	public final void setCheckExpression(String expression) {
		m_checkExpression = expression;
	}
	
    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        StringBuffer b = new StringBuffer();

        b.append("constraint ");
        b.append(m_name);

        switch (m_type) {
        case PRIMARY_KEY:
            b.append(" primary key (");
            if (m_columns.size() == 0) {
            	throw new IllegalStateException("Primary key has zero constrained columns... not allowed!");
            }
            b.append(StringUtils.collectionToDelimitedString(m_columns, ", "));
            break;

        case FOREIGN_KEY:
            b.append(" foreign key (");
            if (m_columns.size() == 0) {
            	throw new IllegalStateException("Foreign key has zero constrained columns... not allowed!");
            }
            b.append(StringUtils.collectionToDelimitedString(m_columns, ", "));
            break;
        case CHECK:
        	b.append(" check (");
        	if(m_checkExpression == null || m_checkExpression.length()==0) {
        		throw new IllegalStateException("Check constraint has no check expression... not allowed!");
        	}
        	b.append(m_checkExpression);
        	break;
        }
        
        b.append(")");

        if (m_type == FOREIGN_KEY) {
            b.append(" references ");
            b.append(m_ftable);
            b.append(" (");
            b.append(StringUtils.collectionToDelimitedString(m_fcolumns, ", "));
            b.append(")");
        }

        if ("c".equals(m_fdeltype)) {
            b.append(" on delete cascade");
        } else if ("r".equals(m_fdeltype)) {
            b.append(" on delete restrict");
        } else if ("n".equals(m_fdeltype)) {
            b.append(" on delete set null");
        } else if ("d".equals(m_fdeltype)) {
            b.append(" on delete set default");
        }

        if ("c".equals(m_fupdtype)) {
            b.append(" on update cascade");
        }

        return b.toString();
    }

    private void parse(String constraintSQL) throws Exception {
        Matcher m;

        	
        //Check if the constraint is a primary key constraint
        m = Pattern.compile("(?i)constraint (\\S+) "
        		+ "primary key \\((.*)\\)").matcher(constraintSQL);
        if (m.matches()) {
            setName(m.group(1));
            setType(PRIMARY_KEY);
            String[] columns = m.group(2).split("\\s*,\\s*");
            setColumns(Arrays.asList(columns));
            return;
        }
        
        //Check if the constraint is a check constraint
        m = Pattern.compile("(?i)constraint (\\S+) "
        		+ "check \\((.*)\\)").matcher(constraintSQL);
        if(m.matches()) {
            setName(m.group(1));
        	setType(CHECK);
        	setCheckExpression(m.group(2));
        	return;
        }

        //Finally, check if the constraint is a foreign key constraint (the most complex)
        m = Pattern.compile("(?i)constraint (\\S+)\\s+" // 1
                + "foreign key\\s+\\(([^\\(\\)]+)\\)\\s+" // 2
                + "references\\s+(\\S+)" // 3
                + "(?:\\s+\\(([^\\(\\)]+)\\))?" // 4
                + "(\\s+on\\s+delete\\s+(?:(cascade)|(restrict)|(set\\s+null)|(set\\s+default)))?" // 5,6,7,8,9
                + "(\\s+on\\s+update\\s+cascade)?").matcher(constraintSQL); // 10
        if (!m.matches()) {
            throw new Exception("Cannot parse constraint: " + constraintSQL);
        }
        	
        setName(m.group(1));
        setType(FOREIGN_KEY);
        String[] columns = m.group(2).split("\\s*,\\s*");
        setColumns(Arrays.asList(columns));
        setForeignTable(m.group(3));
        String[] foreignColumns;
        if (m.group(4) == null) {
            foreignColumns = m.group(2).split("\\s*,\\s*");
        } else {
            foreignColumns = m.group(4).split("\\s*,\\s*");
        }
        setForeignColumns(Arrays.asList(foreignColumns));
        if (m.group(5) == null) {
            setForeignDelType("a");
        } else if (m.group(6) != null) {
            setForeignDelType("c");
        } else if (m.group(7) != null) {
            setForeignDelType("r");
        } else if (m.group(8) != null) {
            setForeignDelType("n");
        } else if (m.group(9) != null) {
            setForeignDelType("d");
        } else {
            throw new Exception("Invalid on delete constraint: "+m.group(5)+": for constraint: "+constraintSQL);
        }
        if (m.group(10) == null) {
            setForeignUpdType("a");
        } else {
            setForeignUpdType("c");
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object other_o) {
        return equals(other_o, false);
    }

    /**
     * <p>equals</p>
     *
     * @param other_o a {@link java.lang.Object} object.
     * @param ignoreFdelType a boolean.
     * @return a boolean.
     */
    public boolean equals(final Object other_o, boolean ignoreFdelType) {
    	if (other_o == null || !(other_o instanceof Constraint)) return false;
    	final Constraint other = (Constraint) other_o;

        if ((m_name == null && other.getName() != null) || (m_name != null && other.getName() == null)) {
            return false;
        }
        if (m_name != null && other.getName() != null && !m_name.equals(other.getName())) {
            return false;
        }

        if (m_type != other.getType()) {
            return false;
        }
        
        if ((m_table == null && other.getTable() != null) || (m_table != null && other.getTable() == null)) {
            return false;
        }
        if (m_table != null && other.getTable() != null && !m_table.equals(other.getTable())) {
            return false;
        }

        if ((m_columns == null && other.getColumns() != null) || (m_columns != null && other.getColumns() == null)) {
            return false;
        }
        if (m_columns != null && other.getColumns() != null && !m_columns.equals(other.getColumns())) {
            return false;
        }

        if ((m_ftable == null && other.getForeignTable() != null) || (m_ftable != null && other.getForeignTable() == null)) {
            return false;
        }
        if (m_ftable != null && other.getForeignTable() != null && !m_ftable.equals(other.getForeignTable())) {
            return false;
        }

        if ((m_fcolumns == null && other.getForeignColumns() != null) || (m_fcolumns != null && other.getForeignColumns() == null)) {
            return false;
        }
        if (m_fcolumns != null && other.getForeignColumns() != null && !m_fcolumns.equals(other.getForeignColumns())) {
            return false;
        }

        if (!ignoreFdelType) {
            if ((m_fdeltype == null && other.getForeignDelType() != null) || (m_fdeltype != null && other.getForeignDelType() == null)) {
                return false;
            }
            if (m_fdeltype != null && other.m_fdeltype != null && !m_fdeltype.equals(other.getForeignDelType())) {
                return false;
            }
            if ((m_fupdtype == null && other.getForeignUpdType() != null) || (m_fupdtype != null && other.getForeignUpdType() == null)) {
                return false;
            }
            if (m_fupdtype != null && other.m_fupdtype != null && !m_fupdtype.equals(other.getForeignUpdType())) {
                return false;
            }
        }
        
        if(m_checkExpression != null && other.getCheckExpression()!=null && !m_checkExpression.equals(other.getCheckExpression())) {
        	return false;
        }

        return true;
    }

    /**
     * <p>hashCode</p>
     *
     * @return a int.
     */
    @Override
    public int hashCode() {
        return m_name.hashCode() + Integer.valueOf(m_type).hashCode() + m_columns.hashCode() + m_ftable.hashCode() + m_fcolumns.hashCode() + m_fdeltype.hashCode();
    }


}
