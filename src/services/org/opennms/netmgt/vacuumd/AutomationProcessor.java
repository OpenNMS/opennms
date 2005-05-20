//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
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
//   OpenNMS Licensing       <license@opennms.org>
//   http://www.opennms.org/
//   http://www.opennms.com/
//
// Tab Size = 8

package org.opennms.netmgt.vacuumd;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.DatabaseConnectionFactory;
import org.opennms.netmgt.config.DbConnectionFactory;
import org.opennms.netmgt.config.VacuumdConfigFactory;
import org.opennms.netmgt.config.vacuumd.Automation;
import org.opennms.netmgt.scheduler.ReadyRunnable;
import org.opennms.netmgt.scheduler.Schedule;

/**
 * This class used to process automations configured in
 * the vacuumd-configuration.xml file.  Automations are
 * identified by a name and they reference
 * Triggers and Actions by name, as well.  Autmations also
 * have an interval attribute that determines how often
 * they run. 
 * @author david
 *
 */
public class AutomationProcessor implements ReadyRunnable {

    private Automation m_automation;
    private boolean m_ready = false;
    private Schedule m_schedule;

    /**
     * Public constructor.
     *
     */
    public AutomationProcessor() {
        m_ready = true;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run() {

        long startDate = System.currentTimeMillis();
        if (getLog().isDebugEnabled())
            getLog().debug("Start Scheduled automation "+this);
        
        if (!(m_automation == null)) {
            Runnable r = new Runnable() {
                public void run() {
                    m_ready = false;
                    try {
                        runAutomation(m_automation);
                    } catch (SQLException e) {
                        getLog().warn("Error running automation: "+e.getMessage());
                    } finally {
                        m_ready = true;
                    }
                }
            };
            r.run();
        }

        if (getLog().isDebugEnabled())
            getLog().debug("run: Finished automation "+m_automation.getName()+", started at "+new Date(startDate));
        
    }

    /**
     * This method verifies that the number of rows in the result set of the trigger
     * match the defined operation in the config.  For example, if the user has specified
     * that the trigger-rows = 5 and the operator ">", the automation will only run
     * if the result rows is greater than 5.
     * 
     * @param trigRowCount
     * @param trigOp
     * @param resultRows
     */
    public boolean triggerRowCheck(int trigRowCount, String trigOp, int resultRows) {
        
        if (trigRowCount == 0 || trigOp == null) {
            if (getLog().isDebugEnabled())
                getLog().debug("triggerRowCheck: trigger has no row-count restrictions: operator is: "+trigOp+", row-count is: "+trigRowCount);
            return true;
        }
        
        if (getLog().isDebugEnabled())
            getLog().debug("triggerRowCheck: Verifying trigger resulting row count " +resultRows+" is "+trigOp+" "+trigRowCount);
        
        boolean runAction = false;
        if ("<".equals(trigOp)) {
            if (resultRows < trigRowCount)
                runAction = true;
            
        } else if ("<=".equals(trigOp)) {
            if (resultRows <= trigRowCount)
                runAction = true;
            
        } else if ("=".equals(trigOp)) {
            if (resultRows == trigRowCount)
                runAction = true;
            
        } else if (">=".equals(trigOp)) {
            if (resultRows >= trigRowCount)
                runAction = true;
            
        } else if (">".equals(trigOp)) {
            if (resultRows > trigRowCount)
                runAction = true;
            
        }
        
        if (getLog().isDebugEnabled())
            getLog().debug("Row count verification is: "+runAction);
        
        return runAction;
    }

    /**
     * Called by the run method to execute the sql statements
     * of triggers and actions defined for an automation.  An
     * automation may have 0 or 1 trigger and must have 1 action.
     * If the automation doesn't have a trigger than the action
     * must not contain any tokens. 
     * 
     * @param auto
     * @return
     * @throws SQLException
     */
    public boolean runAutomation(Automation auto) throws SQLException {
        
        if (getLog().isDebugEnabled())
            getLog().debug("runAutomation: "+auto.getName()+" running...");
        
        boolean actionStatus = false;
        boolean hasTrigger = false;

        String triggerSQL = null;

        if (hasTrigger(auto)) {
            triggerSQL = getTriggerSQL(auto);
            hasTrigger = true;
        }

        String actionSQL = getAutomationSQL(auto);
        
        if (getLog().isDebugEnabled()) {
            getLog().debug("runAutomation: "+auto.getName()+" trigger statement is: "+triggerSQL);
            getLog().debug("runAutomation: "+auto.getName()+" action statement is: "+actionSQL);
        }
        
        Collection actionColumns = getTokenizedColumns(actionSQL);
        
        DbConnectionFactory dcf = DatabaseConnectionFactory.getInstance();
        Connection conn = null;
        Statement triggerStatement = null;
        ResultSet triggerResultSet = null;
        int resultRows = 0;
        
        if (getLog().isDebugEnabled())
            getLog().debug("runAutomation: Executing trigger: "+auto.getTriggerName());
            
        try {
            conn = dcf.getConnection();

            if (hasTrigger) {
                //get a scrollable ResultSet so that we can count the rows and move back to the
                //beginning for processing.
                triggerStatement = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
                triggerResultSet = triggerStatement.executeQuery(triggerSQL);
                
                //determine if number of rows required by the trigger row-count and operator were
                //met by the trigger query, if so we'll run the action
                resultRows = countRows(triggerResultSet);
                if (resultRows < 1)
                    return actionStatus;
                int triggerRowCount = VacuumdConfigFactory.getInstance().getTrigger(auto.getTriggerName()).getRowCount();
                String triggerOperator = VacuumdConfigFactory.getInstance().getTrigger(auto.getTriggerName()).getOperator();
                if (!triggerRowCheck(triggerRowCount, triggerOperator, resultRows))
                    return actionStatus;
                
                //Verfiy the trigger ResultSet returned the require columns for the action statement
                if (!resultSetHasRequiredActionColumns(triggerResultSet, actionColumns))
                    return actionStatus;                
            }
            
            PreparedStatement actionStatement = null;
            
            if (getLog().isDebugEnabled())
                getLog().debug("runAutomation: running action: "+auto.getActionName());
            
            try {
                if (hasTrigger) {
                    triggerResultSet.beforeFirst();
                    conn.setAutoCommit(false);
                    
                    //Loop through the select results
                    while (triggerResultSet.next()) {
                        
                        //Convert the sql to a PreparedStatement
                        actionStatement = convertActionToPreparedStatement(triggerResultSet, actionSQL, conn);
                        actionStatement.executeUpdate();            
                    }
                } else {
                    
                    //No trigger defined, just running the action.
                    if (getTokenCount(actionSQL) != 0) {
                            getLog().info("runAutomation: not running action: "+auto.getActionName()+".  Action contains tokens in an automation ("+auto.getName()+") with no trigger.");
                        return actionStatus;
                    }
                    actionStatement = convertActionToPreparedStatement(triggerResultSet, actionSQL, conn);
                    actionStatement.executeUpdate();
                }
                
                conn.commit();
                
            } catch (SQLException e) {
                conn.rollback();
                getLog().warn("runAutomation: Could not execute update on action: "+auto.getActionName());
                getLog().warn(e.getMessage());
            } finally {
                if (actionStatement != null) {
                    getLog().debug("runAutomation: closing action statement.");
                    actionStatement.close();                    
                }
            }
            
            actionStatus = true;
            
        } catch (SQLException e) {
            getLog().warn("runAutomation: Could not execute trigger: "+auto.getTriggerName());
            getLog().warn(e.getMessage());
        } finally {
            getLog().debug("runAutomation: Closing trigger resultset.");
            if (hasTrigger) {
                getLog().debug("runAutomation: Closing trigger statement.");
                triggerResultSet.close();
            }
            getLog().debug("runAutomation: Closing database connection.");
            conn.close();
        }
        
        return actionStatus;
    }

    private String getAutomationSQL(Automation auto) {
        return VacuumdConfigFactory.getInstance().getAction(auto.getActionName()).getStatement().getContent();
    }

    private String getTriggerSQL(Automation auto) {
        return VacuumdConfigFactory.getInstance().getTrigger(auto.getTriggerName()).getStatement().getContent();
    }

    private boolean hasTrigger(Automation auto) {
        return auto.getTriggerName() != null;
    }

    /**
     * Helper method that verifies tokens in a config defined action
     * are available in the ResultSet of the paired trigger
     * @param rs
     * @param actionSQL
     * @return
     */
    public boolean resultSetHasRequiredActionColumns(ResultSet rs, String actionSQL) {
        Collection actionColumns = getTokenizedColumns(actionSQL);
        return resultSetHasRequiredActionColumns(rs, actionColumns);
    }

    /**
     * Helper method that verifies tokens in a config defined action
     * are available in the ResultSet of the paired trigger
     * @param rs
     * @param actionSQL
     * @return
     */
    public boolean resultSetHasRequiredActionColumns(ResultSet rs, Collection actionColumns) {
        
        if (getLog().isDebugEnabled())
            getLog().debug("resultSetHasRequiredActionColumns: Verifying required action columns in trigger ResultSet...");
        
        boolean verified = false;
        String actionColumnName = null;
        
        Iterator it = actionColumns.iterator();
        
        while (it.hasNext()) {
            actionColumnName = (String)it.next();
            try {
                if (rs.findColumn(actionColumnName) > 0) {
                    verified = true;
                }
            } catch (SQLException e) {
                getLog().warn("resultSetHasRequiredActionColumns: Trigger ResultSet does NOT have required action columns.  Missing: "+actionColumnName);
                getLog().warn(e.getMessage());
                verified = false;
            }
        }

        return verified;
    }

    /**
     * Returns an ArrayList containing the names of column defined
     * as tokens in the action statement defined in the config.  If no
     * tokens are found, an empty list is returned.
     * @param targetString
     * @return
     */
    public Collection getTokenizedColumns(String targetString) {
        // The \w represents a "word" charactor
        String expression = "\\$\\{(\\w+)\\}";
        Pattern pattern = Pattern.compile(expression);
        Matcher matcher = pattern.matcher(targetString);
        
        if (getLog().isDebugEnabled())
            getLog().debug("getTokenizedColumns: processing string: "+targetString);
        
        Collection tokens = new ArrayList();
        int count = 0;
        while (matcher.find()) {
            count++;
            if (getLog().isDebugEnabled())
                getLog().debug("getTokenizedColumns: Token "+count+": "+matcher.group(1));
            
            tokens.add(matcher.group(1));
        }
        return tokens;        
    }

    /**
     * Counts the number of tokens in an Action Statement.
     * @param targetString
     * @return
     */
    public int getTokenCount(String targetString) {
        // The \w represents a "word" charactor
        String expression = "(\\$\\{\\w+\\})";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(targetString);
        
        if (getLog().isDebugEnabled())
            getLog().debug("getTokenCount: processing string: "+targetString);
        
        int count = 0;
        while (matcher.find()) {
            count++;
            if (getLog().isDebugEnabled())
                getLog().debug("getTokenCount: Token "+count+": "+matcher.group(1));
        }
        return count;
    }

    /**
     * Method used to count the rows in a ResultSet.  This probably requires
     * that your ResultSet is scrollable.
     * @param rs
     * @return
     * @throws SQLException
     */
    public int countRows(ResultSet rs) throws SQLException {
        int rows = 0;
        while (rs.next())
            rows++;
        rs.beforeFirst();
        return rows;
    }

    /**
     * This method takes the tokenized action statements from the config
     * and converts it to a PreparedStatment.  The '${token}' format 
     * and changes it to '?' for the statement.  Then the column names 
     * in each token are set from values in the ResultSet of the trigger. 
     * @param rs
     * @param actionSQL
     * @param conn
     * @return
     * @throws SQLException
     */
    public PreparedStatement convertActionToPreparedStatement(ResultSet rs, String actionSQL, Connection conn) throws SQLException {
    
        String actionJDBC = actionSQL.replaceAll("\\$\\{\\w+\\}", "?");
        
        if (getLog().isDebugEnabled())
            getLog().debug("convertActionToPreparedStatement: This action SQL: "+actionSQL+"\nTurned into this: "+actionJDBC);
        
        PreparedStatement stmt = conn.prepareStatement(actionJDBC);
        
        ArrayList actionColumns = (ArrayList)getTokenizedColumns(actionSQL);        
        Iterator it = actionColumns.iterator();
        String actionColumnName = null;
        int colType;
        int i=0;
        while (it.hasNext()) {
            actionColumnName = (String)it.next();
            stmt.setObject(++i, rs.getObject(actionColumnName));
        }
    
        return stmt;
    }

    /**
     * Simple helper method to determine if the targetString contains
     * any '${token}'s.
     * @param targetString
     * @return
     */
    public boolean containsTokens(String targetString) {
        return getTokenCount(targetString) > 0;
    }

    /**
     * @return Returns the automation.
     */
    public Automation getAutomation() {
        return m_automation;
    }
    
    /**
     * @param automation The automation to set.
     */
    public void setAutomation(Automation automation) {
        m_automation = automation;
    }

    public boolean isReady() {
        // TODO Auto-generated method stub
        return m_ready;
    }

    /**
     * @return Returns the schedule.
     */
    public Schedule getSchedule() {
        return m_schedule;
    }
    

    /**
     * @param schedule The schedule to set.
     */
    public void setSchedule(Schedule schedule) {
        m_schedule = schedule;
    }
    
    private Category getLog() {
        return ThreadCategory.getInstance(getClass());        
    }

}
