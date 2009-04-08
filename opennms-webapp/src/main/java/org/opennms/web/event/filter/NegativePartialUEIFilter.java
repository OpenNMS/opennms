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

import org.opennms.web.filter.NegativePartialFilter;
import org.opennms.web.filter.SQLType;

/** Encapsulates filtering on partial unique event identifiers. */
public class NegativePartialUEIFilter extends NegativePartialFilter<String> implements Filter {
    public static final String TYPE = "partialUeiNot";

    protected String uei;

    public NegativePartialUEIFilter(String uei) {
        super(SQLType.STRING, "EVENTUEI", "eventUei", uei, "partialUeiNot");

        this.uei = uei;
    }

//    public String getSql() {
//        return (" LOWER(EVENTUEI) NOT LIKE '%" + this.uei.toLowerCase() + "%'");
//    }
//    
//    public String getParamSql() {
//        return (" LOWER(EVENTUEI) NOT LIKE ?");
//    }
//    
//    public int bindParam(PreparedStatement ps, int parameterIndex) throws SQLException {
//    	ps.setString(parameterIndex, "%"+this.uei.toLowerCase()+"%");
//    	return 1;
//    }
//
//    public String getDescription() {
//        return (TYPE + "=" + this.uei);
//    }
//
//    public String getTextDescription() {
//        return ("partial UEI not like " + this.uei);
//    }
//
//    public String toString() {
//        return ("<EventFactory.NegativePartialUEIFilter: " + this.getDescription() + ">");
//    }
//
//    public String getUEI() {
//        return (this.uei);
//    }
//
//    public boolean equals(Object obj) {
//        return (this.toString().equals(obj.toString()));
//    }
}
