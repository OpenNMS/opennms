//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// The code in this file is Copyright (C) 2004 DJ Gregor.
//
// Based on install.pl which was Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//

package org.opennms.install;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Constraint {
    public static final int PRIMARY_KEY = 1;

    public static final int FOREIGN_KEY = 2;

    private String m_name;

    private int m_type;
    
    private String m_table;

    private List<String> m_columns;

    private String m_ftable;

    private List<String> m_fcolumns;

    private String m_fdeltype;

    private String m_fupdtype;

    public Constraint(String table, String constraint) throws Exception {
        this.parse(constraint);
        this.setTable(table);
    }

    /*
    public Constraint(String table, String name, String column) {
        setTable(table);
        setName(name);
        setType(PRIMARY_KEY);
        setColumn(column);
    }
    */
    
    public Constraint(String table, String name, List<String> columns) {
        setTable(table);
        setName(name);
        setType(PRIMARY_KEY);
        setColumns(columns);
    }

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

    public void setForeignUpdType(String fupdtype) {
        m_fupdtype = fupdtype;
    }
    
    public String getForeignUpdType() {
        return m_fupdtype;
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
    
    public boolean isPrimaryKeyConstraint() {
    	return m_type == PRIMARY_KEY;
    }
    
    public boolean isForeignKeyConstraint() {
    	return m_type == FOREIGN_KEY;
    }
    
	public String getTable() {
		return m_table;
	}

	public void setTable(String table) {
		m_table = table;
	}
    
    public void setColumns(List<String> columns) {
    	m_columns = new ArrayList<String>(columns.size());
    	for (String i : columns) {
    		m_columns.add(i.toLowerCase());
    	}
    }

    public List<String> getColumns() {
        return m_columns;
    }

    public void setColumn(String column) {
    	List<String> columns = new ArrayList<String>(1);
        columns.add(column.toLowerCase());
        setColumns(columns);
    }

    public String getForeignTable() {
        return m_ftable;
    }

    public void setForeignTable(String ftable) {
        m_ftable = ftable.toLowerCase();
    }

    public List<String> getForeignColumns() {
        return m_fcolumns;
    }

    public void setForeignColumn(String fcolumn) {
    	List<String> fcolumns = new ArrayList<String>(1);
    	fcolumns.add(fcolumn.toLowerCase());
        setForeignColumns(fcolumns);
    }

    public void setForeignColumns(List<String> fcolumns) {
    	List<String> newFcolumns = new ArrayList<String>(fcolumns.size());
    	for (String fcolumn : fcolumns) {
    		newFcolumns.add(fcolumn.toLowerCase());
    	}
        m_fcolumns = newFcolumns;
    }

    public String getForeignDelType() {
        return m_fdeltype;
    }

    public void setForeignDelType(String fdeltype) throws Exception {
        if (fdeltype.equals("a") || fdeltype.equals("c")) {
            m_fdeltype = fdeltype;
        } else {
            throw new Exception("confdeltype \"" + fdeltype + "\" unknown");
        }
    }

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
            b.append(Installer.join(", ", m_columns));
            break;

        case FOREIGN_KEY:
            b.append(" foreign key (");
            if (m_columns.size() == 0) {
            	throw new IllegalStateException("Foreign key has zero constrained columns... not allowed!");
            }
            b.append(Installer.join(", ", m_columns));
            break;
        }
        
        b.append(")");

        if (m_type == FOREIGN_KEY) {
            b.append(" references ");
            b.append(m_ftable);
            b.append(" (");
            b.append(Installer.join(", ", m_fcolumns));
            b.append(")");
        }

        if ("c".equals(m_fdeltype)) {
            b.append(" on delete cascade");
        }

        if ("c".equals(m_fupdtype)) {
            b.append(" on update cascade");
        }

        return b.toString();
    }

    private void parse(String constraintSQL) throws Exception {
        Matcher m;

        m = Pattern.compile("(?i)constraint (\\S+) "
        		+ "primary key \\((.*)\\)").matcher(constraintSQL);
        if (m.matches()) {
            setName(m.group(1));
            setType(PRIMARY_KEY);
            String[] columns = m.group(2).split("\\s*,\\s*");
            setColumns(Arrays.asList(columns));
            return;
        }

        m = Pattern.compile("(?i)constraint (\\S+)\\s+"
			    + "foreign key\\s+\\(([^\\(\\)]+)\\)\\s+"
			    + "references\\s+(\\S+)"
			    + "(?:\\s+\\(([^\\(\\)]+)\\))?"
			    + "(\\s+on\\s+delete\\s+cascade)?"
                            + "(\\s+on\\s+update\\s+cascade)?").matcher(constraintSQL);
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
        } else {
            setForeignDelType("c");
        }
        if (m.group(6) == null) {
            setForeignUpdType("a");
        } else {
            setForeignUpdType("c");
        }
    }

    public boolean equals(Object other_o) {
        return equals(other_o, false);
    }

    public boolean equals(Object other_o, boolean ignoreFdelType) {
        Constraint other = (Constraint) other_o;

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

        return true;
    }

    public int hashCode() {
        return m_name.hashCode() + new Integer(m_type).hashCode() + m_columns.hashCode() + m_ftable.hashCode() + m_fcolumns.hashCode() + m_fdeltype.hashCode();
    }

}
