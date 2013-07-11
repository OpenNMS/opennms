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

package org.opennms.netmgt.poller.monitors;

import java.sql.Statement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.Map;
import java.util.HashMap;

import org.opennms.core.utils.ParameterMap;
import org.opennms.netmgt.model.PollStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * <p>JDBCQueryMonitor class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public final class JDBCQueryMonitor extends JDBCMonitor {
    
    
    public static final Logger LOG = LoggerFactory.getLogger(JDBCQueryMonitor.class);

    private static final int OPERATOR_MAP_EQUALS = 0;
    private static final int OPERATOR_MAP_LESS_THAN = 1;
    private static final int OPERATOR_MAP_GREATER_THAN = 2;
    private static final int OPERATOR_MAP_NOT_EQUAL = 3;
    private static final int OPERATOR_MAP_LESS_THAN_EQUAL_TO = 4;
    private static final int OPERATOR_MAP_GREATER_THAN_EQUAL_TO = 5;
    
    private static final int QUERY_ACTION_ROW_COUNT = 0 ;
    private static final int QUERY_ACTION_COMPARE_STRING = 1;
    private static final int QUERY_ACTION_COMPARE_INT = 2;
    private static final int QUERY_ACTION_COMPARE_BOOLEAN = 3;
    
    private static  Map <String,Integer>operatorMap = new HashMap<String, Integer>();
    private static  Map<String, Integer> actionMap = new HashMap<String, Integer>();

    /**
     * <p>Constructor for JDBCQueryMonitor.</p>
     *
     * @throws java.lang.ClassNotFoundException if any.
     * @throws java.lang.InstantiationException if any.
     * @throws java.lang.IllegalAccessException if any.
     */
    public JDBCQueryMonitor() throws ClassNotFoundException,
            InstantiationException, IllegalAccessException {
        
        super();
        operatorMap.put("=", OPERATOR_MAP_EQUALS);
        operatorMap.put("<", OPERATOR_MAP_LESS_THAN);
        operatorMap.put(">", OPERATOR_MAP_GREATER_THAN);
        operatorMap.put("!=",OPERATOR_MAP_NOT_EQUAL);
        operatorMap.put("<=",OPERATOR_MAP_LESS_THAN_EQUAL_TO);
        operatorMap.put(">=",OPERATOR_MAP_GREATER_THAN_EQUAL_TO);   
        
        actionMap.put( "row_count",      QUERY_ACTION_ROW_COUNT);
        actionMap.put( "compare_string", QUERY_ACTION_COMPARE_STRING);
        actionMap.put( "compare_int",    QUERY_ACTION_COMPARE_INT);
        actionMap.put( "compare_bool",   QUERY_ACTION_COMPARE_BOOLEAN);
           
               
    }
    
    /** {@inheritDoc} */
    @Override
    public PollStatus checkDatabaseStatus(Connection con, Map<String, Object> parameters) {
        PollStatus ps = PollStatus.unavailable();
        Statement st = null; 
        String query = ParameterMap.getKeyedString(parameters, "query", null);
        String action = ParameterMap.getKeyedString(parameters, "action", "row_count");
        String column = ParameterMap.getKeyedString(parameters, "column", null);
        String operator = ParameterMap.getKeyedString(parameters, "operator", ">=");
        String message = ParameterMap.getKeyedString(parameters, "message", null );
        
        LOG.debug("Query: {}", query);
        
        if (query == null) {
            ps = PollStatus.unavailable("Null Query, ensure query value set in poller configuration.");
            return ps;
        }
        
        try {
            st = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet rs = st.executeQuery(query);
            rs.first();
            switch(actionMap.get(action).intValue()) {
                case QUERY_ACTION_ROW_COUNT:
                    rs.last();
                    int rowCount = rs.getRow();
                    int expectedRowCount = ParameterMap.getKeyedInteger(parameters,"operand",1);
                    if (integerCheck(rowCount,expectedRowCount,operator))
                        ps = PollStatus.available();
                    else 
                        ps = PollStatus.unavailable("Row Count Check Failed: " + rowCount +  " " +  operator + " " + expectedRowCount );
                    break;
                case QUERY_ACTION_COMPARE_STRING:
                    String expectedString = ParameterMap.getKeyedString(parameters, "operand", null);
                    String retrivedString = rs.getString(column);
                    if ( expectedString.equals(retrivedString)) 
                        ps = PollStatus.available();
                    else
                        ps = PollStatus.unavailable("String Field Check Failed: Expected: " + expectedString + " Returned: " + retrivedString );
                       
                    break;
                case QUERY_ACTION_COMPARE_INT:
                    int expectedInt = ParameterMap.getKeyedInteger(parameters, "operand", 1);
                    int retrivedInt = rs.getInt(column);
                    if (integerCheck(retrivedInt,expectedInt,operator))
                         ps = PollStatus.available();
                    else 
                         ps = PollStatus.unavailable("Integer Field Check Failed: " + expectedInt + " " + operator + " " + retrivedInt  );
                    break;
                
            }
            
        }
        catch ( SQLException sqle ){
            ps = PollStatus.unavailable("Err: " + sqle.toString());
            
        }
        catch ( Exception exp ){ 
            ps = PollStatus.unavailable("Err: " + exp.toString());
            
        }
        finally {
           closeStmt(st);
        }
      
        if(message != null && ps.isUnavailable()) 
            ps = PollStatus.unavailable(message + " " + ps.getReason());
        
        
        return ps;
    }
    
private boolean integerCheck(int val, int expected, String operator){        
        
        switch(operatorMap.get(operator).intValue()){
        case OPERATOR_MAP_EQUALS:
            return val == expected;
        case OPERATOR_MAP_GREATER_THAN:
            return val > expected;
        case OPERATOR_MAP_GREATER_THAN_EQUAL_TO:
            return val >= expected;
        case OPERATOR_MAP_LESS_THAN:
            return val < expected;
        case OPERATOR_MAP_LESS_THAN_EQUAL_TO:
            return val <= expected;
        case OPERATOR_MAP_NOT_EQUAL:
            return val != expected;
        
        }
        
        return false;
        
    }
    
}
