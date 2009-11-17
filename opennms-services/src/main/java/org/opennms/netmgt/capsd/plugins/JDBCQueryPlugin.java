package org.opennms.netmgt.capsd.plugins;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import org.opennms.core.utils.ParameterMap;


public class JDBCQueryPlugin extends JDBCPlugin {
    
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
        
        catch (Exception exp) {
            return false;
        }
        finally {
            closeStmt(st);
        }
        
        return false;
    }
    
}
