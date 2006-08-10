//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.netmgt.dao.jdbc.node;

import java.sql.Types;
import java.util.Collection;
import java.util.Iterator;

import javax.sql.DataSource;

import org.springframework.jdbc.core.SqlParameter;

public class FindByVarCharAssetColumnAndCategoryList extends NodeMappingQuery {

    public FindByVarCharAssetColumnAndCategoryList(DataSource ds, String columnName, Collection<String> categoryNames) {
        super(ds, "from node as n join assets a on (a.nodeid = n.nodeid) " +
                "join category_node cn on (cn.nodeid = n.nodeid) " +
                "join categories c on (c.categoryid = cn.categoryid) " +
                "where c.categoryName in ("+convertCollectionToDelimitedString(categoryNames)+ ")" +
                "and a."+columnName+" = ?");
        declareParameter(new SqlParameter(columnName, Types.VARCHAR));
        compile();
    }
    
    private static String convertCollectionToDelimitedString(Collection<String> col) {
        StringBuffer sb = new StringBuffer("");
        
        for (Iterator it = col.iterator(); it.hasNext();) {
            String colStr = (String) it.next();
            sb.append('\'');
            sb.append(colStr);
            sb.append('\'');
            if (it.hasNext()) {
                sb.append(',');
            }
        }
        return sb.toString();
    }

}