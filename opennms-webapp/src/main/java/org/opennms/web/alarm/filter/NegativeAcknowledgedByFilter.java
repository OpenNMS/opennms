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

package org.opennms.web.alarm.filter;

import org.opennms.web.filter.NegativeFilter;
import org.opennms.web.filter.SQLType;

/** Encapsulates filtering on exact unique event identifiers. */
public class NegativeAcknowledgedByFilter extends NegativeFilter<String> implements Filter {
    public static final String TYPE = "acknowledgedByNot";

    protected String user;

    public NegativeAcknowledgedByFilter(String user) {
        super(SQLType.STRING, "ALARMACKUSER", "alarmAckUser", user, "acknowledgeByNot");
    }

//    public String getSql() {
//        return (" (ALARMACKUSER<>'" + this.user + "' OR ALARMACKUSER IS NULL)");
//    }
    
//    public String getParamSql() {
//        return (" (ALARMACKUSER<>? OR ALARMACKUSER IS NULL)");
//    }
    
//    public int bindParam(PreparedStatement ps, int parameterIndex) throws SQLException {
//    	ps.setString(parameterIndex, this.user);
//    	return 1;
//    }

//    public String getDescription() {
//        return (TYPE + "=" + this.user);
//    }
//
//    public String getTextDescription() {
//        return ("not acknowledged by " + this.user);
//    }

//    public String toString() {
//        return ("<AlarmFactory.NegativeAcknowledgedByFilter: " + this.getDescription() + ">");
//    }
//
//    public String getAcknowledgedByFilter() {
//        return (this.user);
//    }

//    public boolean equals(Object obj) {
//        return (this.toString().equals(obj.toString()));
//    }
//
//    public void applyCriteria(OnmsCriteria criteria) {
//        // TODO Auto-generated method stub
//        
//    }
}
