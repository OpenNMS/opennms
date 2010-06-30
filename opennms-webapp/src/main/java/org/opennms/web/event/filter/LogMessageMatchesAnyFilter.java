//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2007 Jul 24: Java 5 generics and format code. - dj@opennms.org
// 2004 Feb 11: Change the search string logic from 'OR' to 'AND'.
//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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

package org.opennms.web.event.filter;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * <p>LogMessageMatchesAnyFilter class.</p>
 *
 * @author <A HREF="mailto:jamesz@opennms.com">James Zuo </A>
 * @version $Id: $
 * @since 1.6.12
 */
public class LogMessageMatchesAnyFilter extends Object implements Filter {
    /** Constant <code>TYPE="msgmatchany"</code> */
    public static final String TYPE = "msgmatchany";

    protected String[] substrings;

    /**
     * <p>Constructor for LogMessageMatchesAnyFilter.</p>
     *
     * @param stringList
     *            a space-delimited list of search substrings
     */
    public LogMessageMatchesAnyFilter(String stringList) {
        if (stringList == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        StringTokenizer tokenizer = new StringTokenizer(stringList);
        List<String> list = new ArrayList<String>();

        while (tokenizer.hasMoreTokens()) {
            list.add(tokenizer.nextToken());
        }

        if (list.size() == 0) {
            throw new IllegalArgumentException("Cannot take a zero-length list of substrings");
        }

        this.substrings = list.toArray(new String[list.size()]);
    }

    /**
     * <p>Constructor for LogMessageMatchesAnyFilter.</p>
     *
     * @param substrings an array of {@link java.lang.String} objects.
     */
    public LogMessageMatchesAnyFilter(String[] substrings) {
        if (substrings == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        this.substrings = substrings;
    }

    /**
     * <p>getSql</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSql() {
        StringBuffer buffer = new StringBuffer(" (");

        buffer.append("UPPER(EVENTLOGMSG) LIKE '%");
        buffer.append(getQueryString().toUpperCase());
        buffer.append("%'");
        buffer.append(")");

        return buffer.toString();
    }
    
    /**
     * <p>getParamSql</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getParamSql() {
        return (" UPPER(EVENTLOGMSG) LIKE ?");
    }
    
    /** {@inheritDoc} */
    public int bindParam(PreparedStatement ps, int parameterIndex) throws SQLException {
    	ps.setString(parameterIndex, "%"+getQueryString().toUpperCase()+"%");
    	return 1;
    }

    /**
     * <p>getDescription</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDescription() {
        return TYPE + "=" + this.getQueryString();
    }

    /**
     * <p>getTextDescription</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getTextDescription() {
        StringBuffer buffer = new StringBuffer("message containing \"");
        buffer.append(getQueryString());
        buffer.append("\"");

        return buffer.toString();
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String toString() {
        return "<LogMessageMatchesAnyFilter: " + this.getDescription() + ">";
    }

    /**
     * <p>Getter for the field <code>substrings</code>.</p>
     *
     * @return an array of {@link java.lang.String} objects.
     */
    public String[] getSubstrings() {
        return this.substrings;
    }

    /** {@inheritDoc} */
    public boolean equals(Object obj) {
        return this.toString().equals(obj.toString());
    }

    /**
     * <p>getQueryString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getQueryString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(this.substrings[0]);

        for (int i = 1; i < this.substrings.length; i++) {
            buffer.append(" ");
            buffer.append(this.substrings[i]);
        }

        return buffer.toString();
    }

}
