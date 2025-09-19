/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.poller.monitors;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.opennms.core.utils.ParameterMap;
import org.opennms.netmgt.poller.PollStatus;
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
    
    private static final Map <String,Integer>operatorMap = new HashMap<String, Integer>();

    static {
        operatorMap.put("=", OPERATOR_MAP_EQUALS);
        operatorMap.put("<", OPERATOR_MAP_LESS_THAN);
        operatorMap.put(">", OPERATOR_MAP_GREATER_THAN);
        operatorMap.put("!=",OPERATOR_MAP_NOT_EQUAL);
        operatorMap.put("<=",OPERATOR_MAP_LESS_THAN_EQUAL_TO);
        operatorMap.put(">=",OPERATOR_MAP_GREATER_THAN_EQUAL_TO);
    }

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
    }
    
    /** {@inheritDoc} */
    @Override
    public PollStatus checkDatabaseStatus(Connection con, Map<String, Object> parameters) {
        PollStatus ps = PollStatus.unavailable();
        Statement st = null;
        final String query = ParameterMap.getKeyedString(parameters, "query", null);
        final String action = ParameterMap.getKeyedString(parameters, "action", "row_count");
        final String column = ParameterMap.getKeyedString(parameters, "column", null);
        final String operator = ParameterMap.getKeyedString(parameters, "operator", ">=");
        final String message = ParameterMap.getKeyedString(parameters, "message", null );
        final String lineSeparator = ParameterMap.getKeyedString(parameters, "line-separator", "\n");
        final String columnSeparator = ParameterMap.getKeyedString(parameters, "column-separator", "; ");
        final String columnKeySeparator = ParameterMap.getKeyedString(parameters, "column-key-separator", ": ");
        final boolean includeFirstResult = ParameterMap.getKeyedBoolean(parameters, "include-first-result", false);
        final boolean includeAllResults = ParameterMap.getKeyedBoolean(parameters, "include-all-results", false);
        final boolean includeQuery = ParameterMap.getKeyedBoolean(parameters, "include-query", false);
        
        LOG.debug("Query: {}", query);
        
        if (query == null) {
            ps = PollStatus.unavailable("Null Query, ensure query value set in poller configuration.");
            return ps;
        }
        
        try {
            st = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
            final ResultSet rs = st.executeQuery(query);
            rs.first();
            ResultsMessage resultsMessage = null;
            switch(action) {
                case "row_count":
                    int rowCount = 0;
                    if (includeAllResults || includeFirstResult) {
                        // capture the results regardless of polling outcome since we might have a TYPE_FORWARD_ONLY driver
                        resultsMessage = buildResultsMessage(rs, includeAllResults, lineSeparator, columnSeparator, columnKeySeparator);
                        // can't call `rs.last()` (safely) a second time, so use resultsMessage.lines() to derive rowCount
                        if (includeAllResults) {
                            rowCount = resultsMessage.rows;
                        }
                    }
                    if (!includeAllResults) {
                        rs.last();
                        rowCount = rs.getRow();
                    }
                    final int expectedRowCount = ParameterMap.getKeyedInteger(parameters,"operand",1);
                    if (integerCheck(rowCount,expectedRowCount,operator)) {
                        ps = PollStatus.available();
                    } else {
                        ps = PollStatus.unavailable("Row Count Check Failed: " + rowCount +  " " +  operator + " " + expectedRowCount );
                        if (includeQuery) {
                            ps.setReason(ps.getReason() + lineSeparator + "Query: " + query);
                        }
                        if (includeAllResults || includeFirstResult) {
                            ps.setReason(ps.getReason() + resultsMessage.message);
                        }
                    }
                    break;
                case "compare_string":
                    final String expectedString = ParameterMap.getKeyedString(parameters, "operand", null);
                    final String retrivedString = rs.getString(column);
                    if ( expectedString.equals(retrivedString)) {
                        ps = PollStatus.available();
                    } else {
                        final StringBuilder mb = new StringBuilder("String Field Check Failed: Expected: ").append(expectedString).append(" Returned: ").append(retrivedString);
                        if (includeQuery) {
                            mb.append(lineSeparator).append("Query: ").append(query);
                        }
                        if (includeAllResults || includeFirstResult) {
                            mb.append(buildResultsMessage(rs, includeAllResults, lineSeparator, columnSeparator, columnKeySeparator).message);
                        }
                        ps = PollStatus.unavailable(mb.toString());
                    }
                    break;
                case "compare_int":
                    final int expectedInt = ParameterMap.getKeyedInteger(parameters, "operand", 1);
                    final int retrivedInt = rs.getInt(column);
                    if (integerCheck(retrivedInt,expectedInt,operator)) {
                        ps = PollStatus.available();
                    } else {
                        final StringBuilder mb = new StringBuilder("Integer Field Check Failed: ").append(expectedInt).append(" ").append(operator).append(" ").append(retrivedInt);
                        if (includeQuery) {
                            mb.append(lineSeparator).append("Query: ").append(query);
                        }
                        if (includeAllResults || includeFirstResult) {
                            mb.append(buildResultsMessage(rs, includeAllResults, lineSeparator, columnSeparator, columnKeySeparator).message);
                        }
                        ps = PollStatus.unavailable(mb.toString());
                    }
                    break;
                case "compare_bool":
                    boolean expectedBool = ParameterMap.getKeyedBoolean(parameters, "operand", true);
                    boolean retrievedBool = rs.getBoolean(column);
                    if (expectedBool == retrievedBool) {
                        ps = PollStatus.available();
                    } else {
                        final StringBuilder mb = new StringBuilder("Boolean Field Check Failed: ").append(expectedBool).append(" does not equal ").append(retrievedBool).append(" found in column ").append(column);
                        if (includeQuery) {
                            mb.append(lineSeparator).append("Query: ").append(query);
                        }
                        if (includeAllResults || includeFirstResult) {
                            mb.append(buildResultsMessage(rs, includeAllResults, lineSeparator, columnSeparator, columnKeySeparator).message);
                        }
                        ps = PollStatus.unavailable(mb.toString());
                    }
                    break;
                default:
                    LOG.error("Unexpected action: {}", action);
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

    private ResultsMessage buildResultsMessage(final ResultSet rs, final boolean includeAllResults, final String lineSeparator, final String columnSeparator, final String columnKeySeparator) throws SQLException {
        ResultsMessage ret;
        int rows = 0;
        final StringBuilder resultsMessage = new StringBuilder(lineSeparator).append("Results:");
        final ResultSetMetaData rsmd = rs.getMetaData();
        boolean first = true;
        while (first || rs.next()) {
            first = false;
            if (rs.getRow() > 1 && !includeAllResults)
                break;
            rows++;
            for(int i = 1; i <= rsmd.getColumnCount(); i++) {
                if (i == 1) {
                    resultsMessage.append(lineSeparator);
                } else {
                    resultsMessage.append(columnSeparator);
                }
                resultsMessage.append(rsmd.getColumnName(i)).append(columnKeySeparator).append(rs.getString(i));
            }
        }
        return new ResultsMessage(resultsMessage.toString(), rows);
    }
    
    private final class ResultsMessage {
        public final String message;
        public final int rows;
        public ResultsMessage(String message, int rows){
            super();
            this.message = message;
            this.rows = rows;
        }
    }
}
