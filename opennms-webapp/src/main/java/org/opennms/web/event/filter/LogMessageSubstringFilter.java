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

import org.opennms.web.filter.PartialFilter;
import org.opennms.web.filter.SQLType;

public class LogMessageSubstringFilter extends PartialFilter<String> implements Filter {
    public static final String TYPE = "msgsub";

    protected String substring;

    public LogMessageSubstringFilter(String substring) {
        super(SQLType.STRING, "EVENTLOGMSG", "eventLogMsg", substring, "msgsub");

        this.substring = substring;
    }

//    public String getSql() {
//        return (" UPPER(EVENTLOGMSG) LIKE '%" + this.substring.toUpperCase() + "%'");
//    }
//    
//    public String getParamSql() {
//        return (" UPPER(EVENTLOGMSG) LIKE ?");
//    }
//    
//    public int bindParam(PreparedStatement ps, int parameterIndex) throws SQLException {
//    	ps.setString(parameterIndex, "%"+this.substring.toUpperCase()+"%");
//    	return 1;
//    }
//
//    public String getDescription() {
//        return (TYPE + "=" + this.substring);
//    }
//
//    public String getTextDescription() {
//        return ("description containing \"" + this.substring + "\"");
//    }
//
//    public String toString() {
//        return ("<LogMessageSubstringFilter: " + this.getDescription() + ">");
//    }
//
//    public String getSubstring() {
//        return (this.substring);
//    }
//
//    public boolean equals(Object obj) {
//        return (this.toString().equals(obj.toString()));
//    }
}
