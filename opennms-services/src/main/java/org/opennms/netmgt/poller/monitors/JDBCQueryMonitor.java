package org.opennms.netmgt.poller.monitors;

import java.sql.Statement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.Map;
import java.util.HashMap;

import org.opennms.core.utils.ParameterMap;
import org.opennms.netmgt.model.PollStatus;



/**
 * <p>JDBCQueryMonitor class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public final class JDBCQueryMonitor extends JDBCMonitor {

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
    public PollStatus checkDatabaseStatus(Connection con, Map<String, Object> parameters) {
        PollStatus ps = PollStatus.unavailable();
        Statement st = null; 
        String query = ParameterMap.getKeyedString(parameters, "query", null);
        String action = ParameterMap.getKeyedString(parameters, "action", "row_count");
        String column = ParameterMap.getKeyedString(parameters, "column", null);
        String operator = ParameterMap.getKeyedString(parameters, "operator", ">=");
        String message = ParameterMap.getKeyedString(parameters, "message", null );
        
        log().debug("Query: " + query);
        
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
