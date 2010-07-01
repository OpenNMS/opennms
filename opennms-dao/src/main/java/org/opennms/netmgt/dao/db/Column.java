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

package org.opennms.netmgt.dao.db;

import java.sql.Types;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>Column class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class Column {
    private String m_name = null;

    private String m_type = null;

    private int m_size = 0;

    private boolean m_notNull = false;
    
    private String m_defaultValue = null;

    /** {@inheritDoc} */
    public boolean equals(Object other_o) {
        Column other = (Column) other_o;

        if ((m_name == null && other.getName() != null) || (m_name != null && other.getName() == null)) {
            return false;
        } else if (m_name != null && other.getName() != null && !m_name.equals(other.getName())) {
            return false;
        } else if ((m_type == null && other.getType() != null) || (m_type != null && other.getType() == null)) {
            return false;
        } else if (m_type != null && other.getType() != null && !m_type.equals(other.getType())) {
            return false;
        } else if (m_size != other.getSize()) {
            return false;
        } else if (m_notNull != other.isNotNull()) {
            return false;
        } else if ((m_defaultValue == null && other.getDefaultValue() != null) || (m_defaultValue != null && other.getDefaultValue() == null)) {
            return false;
        } else if (m_defaultValue != null && other.getDefaultValue() != null && !m_defaultValue.equals(other.getDefaultValue())) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * <p>hashCode</p>
     *
     * @return a int.
     */
    public int hashCode() {
        return m_name.hashCode() + m_type.hashCode() + Integer.valueOf(m_size).hashCode() + Boolean.valueOf(m_notNull).hashCode();
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String toString() {
        StringBuffer b = new StringBuffer();

        b.append(m_name);

        b.append(" ");
        b.append(m_type);
        if (m_size > 0) {
            b.append("(");
            b.append(Integer.toString(m_size));
            if (m_type.equals("numeric")) {
                b.append(",2");
            }
            b.append(")");
        }

        if (hasDefaultValue()) {
            b.append(" DEFAULT " + getDefaultValue());
        }

        if (m_notNull) {
            b.append(" NOT NULL");
        }

        return b.toString();
    }

    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getName() {
        return m_name;
    }

    /**
     * <p>setName</p>
     *
     * @param name a {@link java.lang.String} object.
     */
    public void setName(String name) {
        m_name = name.toLowerCase();
    }

    /**
     * <p>isNotNull</p>
     *
     * @return a boolean.
     */
    public boolean isNotNull() {
        return m_notNull;
    }

    /**
     * <p>setNotNull</p>
     *
     * @param notNull a boolean.
     */
    public void setNotNull(boolean notNull) {
        m_notNull = notNull;
    }

    /**
     * <p>getSize</p>
     *
     * @return a int.
     */
    public int getSize() {
        return m_size;
    }

    /**
     * <p>setSize</p>
     *
     * @param size a int.
     */
    public void setSize(int size) {
        m_size = size;
    }

    /**
     * <p>getType</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getType() {
        return m_type;
    }

    /**
     * <p>setType</p>
     *
     * @param type a {@link java.lang.String} object.
     */
    public void setType(String type) {
        m_type = type;
    }
    
    /**
     * <p>getDefaultValue</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDefaultValue() {
        return m_defaultValue;
    }
    
    /**
     * <p>hasDefaultValue</p>
     *
     * @return a boolean.
     */
    public boolean hasDefaultValue() {
        return m_defaultValue != null;
    }

    /**
     * <p>setDefaultValue</p>
     *
     * @param defaultValue a {@link java.lang.String} object.
     */
    public void setDefaultValue(String defaultValue) {
        if (defaultValue != null && defaultValue.matches("nextval\\('[^']+'\\)")) {
            m_defaultValue = defaultValue.toLowerCase();
        } else {
            m_defaultValue = defaultValue;
        }
    }

    /**
     * <p>parse</p>
     *
     * @param column a {@link java.lang.String} object.
     * @throws java.lang.Exception if any.
     */
    public void parse(String column) throws Exception {
        Matcher m;

        m = Pattern.compile("(?i)(.*)\\bnot null\\b(.*)").matcher(column);
        if (m.matches()) {
            m_notNull = true;
            column = m.group(1) + m.group(2);
        }

        column = column.trim().replaceAll("\\s+", " ");

        m = Pattern.compile("(?i)(.*?)\\s*\\bdefault (.+)").matcher(column);
        if (m.matches()) {
            column = m.group(1);
            setDefaultValue(m.group(2));
        }

        String col_name = null;
        String col_type;

        // m =
        // Pattern.compile("((?:['\"])?\\S+?(?:['\"])?)\\s+((?:['\"])?.+?(?:['\"])?)").matcher(column);
        m = Pattern.compile("(\\S+)\\s+(.+)").matcher(column);
        if (m.matches()) {
            col_name = m.group(1).replaceAll("^['\"]", "").replaceAll("['\"]$", "");
            col_type = m.group(2).replaceAll("^['\"]", "").replaceAll("['\"]$", "");
        } else {
            throw new Exception("cannot parse column: " + column);
        }

        this.setName(col_name);

        parseColumnType(col_type.trim().toLowerCase());
    }

    /**
     * <p>parseColumnType</p>
     *
     * @param columnType a {@link java.lang.String} object.
     * @throws java.lang.Exception if any.
     */
    public void parseColumnType(String columnType) throws Exception {
        int start, end;
        String type, size = null;

        start = columnType.indexOf('(');
        end = columnType.indexOf(')');

        if (start != -1 && end != -1) {
            type = columnType.substring(0, start);
            size = columnType.substring(start + 1, end).replaceAll(",\\d+", "");
        } else {
            type = columnType;
        }

        this.setType(normalizeColumnType(type, size != null));
        if (size != null) {
            this.setSize(Integer.parseInt(size));
        } else {
        	try {
        		this.setSize(columnTypeSize(this.getType()));
        	} catch (Exception e) {
        		throw new Exception("Could not determine size for column " + getName() + ".  Chained: " + e.getMessage(), e);
        	}
        	
        }
    }

    /**
     * <p>getColumnSqlType</p>
     *
     * @return a int.
     * @throws java.lang.Exception if any.
     */
    public int getColumnSqlType() throws Exception {
        if (m_type.equals("integer")) {
            return Types.INTEGER;
        } else if (m_type.equals("smallint")) {
            return Types.SMALLINT;
        } else if (m_type.equals("bigint")) {
            return Types.BIGINT;
        } else if (m_type.equals("real")) {
            return Types.REAL;
        } else if (m_type.equals("double precision")) {
            return Types.DOUBLE;
        } else if (m_type.equals("boolean")) {
            return Types.BOOLEAN;
        } else if (m_type.equals("character")) {
            return Types.CHAR;
        } else if (m_type.equals("character varying")) {
            return Types.VARCHAR;
        } else if (m_type.equals("bpchar")) {
            return Types.VARCHAR;
        } else if (m_type.equals("numeric")) {
            return Types.NUMERIC;
        } else if (m_type.equals("text")) {
            return Types.LONGVARCHAR;
        } else if (m_type.equals("timestamp")) {
            return Types.TIMESTAMP;
        } else if (m_type.equals("timestamptz")) {
            return Types.TIMESTAMP;
        } else if (m_type.equals("bytea")) {
            return Types.VARBINARY;
        } else {
            throw new Exception("Do not have a Java SQL type for \"" + m_type + "\"");
        }
    }

    /**
     * <p>normalizeColumnType</p>
     *
     * @param column a {@link java.lang.String} object.
     * @param hasSize a boolean.
     * @return a {@link java.lang.String} object.
     * @throws java.lang.Exception if any.
     */
    public static String normalizeColumnType(String column, boolean hasSize) throws Exception {
        if (column.equals("integer") || column.equals("int4")) {
            return "integer";
        } else if (column.equals("float") || column.equals("float8") || column.equals("double precision") || column.equals("double")) {
            return "double precision";
        } else if (column.equals("float4") || column.equals("real")) {
            return "real";
        } else if (column.equals("bigint") || column.equals("int8")) {
            return "bigint";
        } else if (column.equals("int2") || column.equals("smallint")) {
            return "smallint";
        } else if (column.equals("bool") || column.equals("boolean")) {
            return "boolean";
        } else if (column.equals("character") && !hasSize) {
            return "character";
        } else if (column.equals("varchar") || column.equals("character varying")) {
            return "character varying";
        } else if ((column.equals("char") || column.equals("character") || column.equals("bpchar")) && hasSize) {
            return "bpchar";
        } else if (column.equals("numeric")) {
            return "numeric";
        } else if (column.equals("text")) {
            return "text";
        } else if (column.equals("trigger")) {
            return "trigger";
        } else if (column.equals("timestamp") || column.equals("timestamp without time zone")) {
            return "timestamp";
        } else if (column.equals("timestamptz") || column.equals("timestamp with time zone")) {
            return "timestamptz";
        } else if (column.equals("bytea")) {
            return "bytea";
        } else {
            throw new Exception("cannot parse column type: '" + column + "'");
        }
    }

    /**
     * <p>columnTypeSize</p>
     *
     * @param type a {@link java.lang.String} object.
     * @return a int.
     * @throws java.lang.Exception if any.
     */
    public int columnTypeSize(String type) throws Exception {
        if (type.equals("boolean") || type.equals("character")) {
            return 1;
        } else if (type.equals("smallint")) {
            return 2;
        } else if (type.equals("integer")) {
            return 4;
        } else if (type.equals("bigint") || type.equals("timestamp") || type.equals("timestamptz")) {
            return 8;
        } else if (type.equals("double precision") || type.equals("real") || type.equals("text") || type.equals("bytea") || type.equals("trigger")) {
            return -1;
        } else {
            throw new Exception("do not know the type size for " + "column type \"" + type + "\"");
        }
    }
}
