/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.capsd.plugins;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import org.opennms.core.utils.ParameterMap;


/**
 * <p>JDBCQueryPlugin class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class JDBCQueryPlugin extends JDBCPlugin {
    
    /** {@inheritDoc} */
    @Override
    public boolean checkStatus(Connection con, Map<String, Object> qualifiers) {
        Statement st = null; 
        String query = ParameterMap.getKeyedString(qualifiers, "query", null);
        
        if(query == null) return false;
        
        try {   
            st = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet rs = st.executeQuery(query);
            rs.first();
            
            if (rs.getRow() == 1)
                return true;
            
        }
        catch(SQLException exp) {
            return false;
            
        }
        
        catch (Throwable exp) {
            return false;
        }
        finally {
            closeStmt(st);
        }
        
        return false;
    }
    
}
