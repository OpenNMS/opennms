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
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.DatabaseConnectionFactory;
import org.opennms.netmgt.config.VacuumdConfigFactory;
import org.opennms.netmgt.config.vacuumd.Automation;
import org.opennms.netmgt.scheduler.ReadyRunnable;
import org.opennms.netmgt.scheduler.Schedule;
import org.opennms.netmgt.xml.event.Event;

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
    private boolean m_triggerSuccessful;
    private boolean m_actionSuccessful;
    private boolean m_triggerInAutomation;
    private Statement m_triggerStatement;
    private ResultSet m_triggerResultSet;
    private Collection m_actionColumns;
    private Connection m_conn;

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
        if (log().isDebugEnabled())
            log().debug("Start Scheduled automation "+this);
        
        if (!(getAutomation() == null)) {
            Runnable r = new Runnable() {
                public void run() {
                    setReady(false);
                    try {
                        runAutomation(getAutomation());
                    } catch (SQLException e) {
                        log().warn("Error running automation: "+getAutomation().getName()+", "+e.getMessage());
                    } finally {
                        setReady(true);
                    }
                }
            };
            r.run();
        }

        if (log().isDebugEnabled())
            log().debug("run: Finished automation "+m_automation.getName()+", started at "+new Date(startDate));
        
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
    public static boolean triggerRowCheck(int trigRowCount, String trigOp, int resultRows) {
        
        if (trigRowCount == 0 || trigOp == null) {
            if (log().isDebugEnabled())
                log().debug("triggerRowCheck: trigger has no row-count restrictions: operator is: "+trigOp+", row-count is: "+trigRowCount);
            return true;
        }
        
        if (log().isDebugEnabled())
            log().debug("triggerRowCheck: Verifying trigger resulting row count " +resultRows+" is "+trigOp+" "+trigRowCount);
        
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
        
        if (log().isDebugEnabled())
            log().debug("Row count verification is: "+runAction);
        
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

        if (log().isDebugEnabled())
            log().debug("runAutomation: "+auto.getName()+" running...");

        setTriggerInAutomation(hasTrigger(auto));

        if (log().isDebugEnabled()) {
            if (isTriggerInAutomation())
                log().debug("runAutomation: "+auto.getName()+" trigger statement is: "+ getTriggerSQL(auto));
            
            log().debug("runAutomation: "+auto.getName()+" action statement is: "+getActionSQL(auto));
        }

        setFields(auto);

        if (log().isDebugEnabled())
            log().debug("runAutomation: Executing trigger: "+auto.getTriggerName());

        try {
            setConn(DatabaseConnectionFactory.getInstance().getConnection());

            processTrigger(auto);
            
            if (!isTriggerSuccessful()) {
                return false;
            }

            PreparedStatement actionStatement = null;

            try {
                setActionSuccessful(false);
                processAction(auto);           
            } catch (SQLException e) {
                getConn().rollback();
                log().warn("runAutomation: Could not execute update on action: "+auto.getActionName());
                log().warn(e.getMessage());
            } finally {
                if (actionStatement != null) {
                    log().debug("runAutomation: closing action statement.");
                    actionStatement.close();                    
                }
            }

        } catch (SQLException e) {
            log().warn("runAutomation: Could not execute trigger: "+auto.getTriggerName(), e);
        } finally {
            log().debug("runAutomation: Closing trigger resultset.");
            if (isTriggerInAutomation()) {
                log().debug("runAutomation: Closing trigger statement.");
                //Just in case, check for null
                if (getTriggerResultSet() != null)
                    getTriggerResultSet().close();
            }
            log().debug("runAutomation: Closing database connection.");
            getConn().close();
        }

        return isActionSuccessful();
    }

    private void processAction(Automation auto) throws SQLException {

        if (log().isDebugEnabled())
            log().debug("runAutomation: running action(s): "+auto.getActionName());

        getConn().setAutoCommit(false);
        if (isTriggerInAutomation()) {
            getTriggerResultSet().beforeFirst();
            
            //Loop through the select results
            while (getTriggerResultSet().next()) {                        
                processActionStatement(getActionSQL(auto), getTriggerResultSet());            
                sendAutoEvent(auto);
            }
            setActionSuccessful(true);
        } else {
            //No trigger defined, just running the action.
            if (getTokenCount(getActionSQL(auto)) != 0) {
                log().info("runAutomation: not running action: "+auto.getActionName()+".  Action contains tokens in an automation ("+auto.getName()+") with no trigger.");
                setActionSuccessful(false);
            } else {
                processActionStatement(getActionSQL(auto), getTriggerResultSet());
                sendAutoEvent(auto);
                setActionSuccessful(true);
            }
        }
        getConn().commit();
    }

    private void processTrigger(Automation auto) throws SQLException {
        if (isTriggerInAutomation()) {
            //get a scrollable ResultSet so that we can count the rows and move back to the
            //beginning for processing.
            setTriggerStatement(getConn().createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY));
            setTriggerResultSet(getTriggerStatement().executeQuery(getTriggerSQL(auto)));

            //Verfiy the trigger ResultSet returned the required number of rows and the required columns for the action statement
            if (!verifyRowCount(auto, getTriggerResultSet()) || !resultSetHasRequiredActionColumns(getTriggerResultSet(), getActionColumns())) {
                setTriggerSuccessful(false);
            } else {
                setTriggerSuccessful(true);
            }
        } else {
            setTriggerSuccessful(true);
        }
    }

    private void setFields(Automation auto) {
        setActionColumns(getTokenizedColumns(getActionSQL(auto)));
        setConn(null);
        setTriggerStatement(null);
        setTriggerResultSet(null);
    }

    private void sendAutoEvent(Automation auto) {

        if (log().isDebugEnabled())
            log().debug("runAutomation: Sending any possible configured event for automation: "+auto.getName());
        
        if (hasEvent(auto)) {
            String uei = getUei(auto);
            //create and send event
            if (log().isDebugEnabled())
                log().debug("runAutomation: Sending event: "+uei+" for automation: "+auto.getName());
            
            Event e = createEvent("Automation", uei);
            Vacuumd.getSingleton().getEventManager().sendNow(e);
        } else {
            if (log().isDebugEnabled())
                log().debug("runAutomation: No event configured automation: "+auto.getName());             
        }
    }

    private String getUei(Automation auto) {
        if (hasEvent(auto)) {
            return VacuumdConfigFactory.getInstance().getAutoEvent(auto.getAutoEventName()).getUei().getContent();
        } else {
            return null;
        }
    }

    private static Event createEvent(String source, String uei) {
        Event event = new Event();
        event.setSource(source);
        event.setUei(uei);
        String eventTime = EventConstants.formatToString(new Date());
        event.setCreationTime(eventTime);
        event.setTime(eventTime);
        return event;
    }

    private boolean verifyRowCount(Automation auto, ResultSet triggerResultSet) throws SQLException {
        int resultRows;
        boolean validRows = true;
        //determine if number of rows required by the trigger row-count and operator were
        //met by the trigger query, if so we'll run the action
        resultRows = countRows(triggerResultSet);
        
        int triggerRowCount = VacuumdConfigFactory.getInstance().getTrigger(auto.getTriggerName()).getRowCount();
        String triggerOperator = VacuumdConfigFactory.getInstance().getTrigger(auto.getTriggerName()).getOperator();

        if (log().isDebugEnabled())
            log().debug("verifyRowCount: Verifying trigger result: "+resultRows+" is "+triggerOperator+" than "+triggerRowCount);

        if (resultRows < 1) {
            validRows = false;
        } else {    
            if (!triggerRowCheck(triggerRowCount, triggerOperator, resultRows))
                validRows = false;
        }

        return validRows;
    }

    private void processActionStatement(String actionSQL, ResultSet triggerResultSet) throws SQLException {
        PreparedStatement actionStatement;
        //Convert the sql to a PreparedStatement
        actionStatement = convertActionToPreparedStatement(triggerResultSet, actionSQL);
        actionStatement.executeUpdate();
    }

    private String getActionSQL(Automation auto) {
        return VacuumdConfigFactory.getInstance().getAction(auto.getActionName()).getStatement().getContent();
    }

    public static String getTriggerSQL(Automation auto) {
        if (hasTrigger(auto)) {
            return VacuumdConfigFactory.getInstance().getTrigger(auto.getTriggerName()).getStatement().getContent();
        } else {
            return null;
        }
    }

    private static boolean hasTrigger(Automation auto) {
        return auto.getTriggerName() != null;
    }
    
    private static boolean hasEvent(Automation auto) {
        return auto.getAutoEventName() != null;
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
        
        if (log().isDebugEnabled())
            log().debug("resultSetHasRequiredActionColumns: Verifying required action columns in trigger ResultSet...");
        
        boolean verified = true;
        String actionColumnName = null;
        
        Iterator it = actionColumns.iterator();
        
        while (it.hasNext()) {
            actionColumnName = (String)it.next();
            try {
                if (rs.findColumn(actionColumnName) > 0) {
                }
            } catch (SQLException e) {
                log().warn("resultSetHasRequiredActionColumns: Trigger ResultSet does NOT have required action columns.  Missing: "+actionColumnName);
                log().warn(e.getMessage());
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
        
        if (log().isDebugEnabled())
            log().debug("getTokenizedColumns: processing string: "+targetString);
        
        Collection tokens = new ArrayList();
        int count = 0;
        while (matcher.find()) {
            count++;
            if (log().isDebugEnabled())
                log().debug("getTokenizedColumns: Token "+count+": "+matcher.group(1));
            
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
        
        if (log().isDebugEnabled())
            log().debug("getTokenCount: processing string: "+targetString);
        
        int count = 0;
        while (matcher.find()) {
            count++;
            if (log().isDebugEnabled())
                log().debug("getTokenCount: Token "+count+": "+matcher.group(1));
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
     * @return
     * @throws SQLException
     */
    public PreparedStatement convertActionToPreparedStatement(ResultSet rs, String actionSQL) throws SQLException {
    
        String actionJDBC = actionSQL.replaceAll("\\$\\{\\w+\\}", "?");
        
        if (log().isDebugEnabled())
            log().debug("convertActionToPreparedStatement: This action SQL: "+actionSQL+"\nTurned into this: "+actionJDBC);
        
        PreparedStatement stmt = getConn().prepareStatement(actionJDBC);
        
        ArrayList actionColumns = (ArrayList)getTokenizedColumns(actionSQL);        
        Iterator it = actionColumns.iterator();
        String actionColumnName = null;
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
    
    private static Category log() {
        return ThreadCategory.getInstance(AutomationProcessor.class);        
    }

    private void setActionSuccessful(boolean actionStatus) {
        m_actionSuccessful = actionStatus;
    }

    private boolean isActionSuccessful() {
        return m_actionSuccessful;
    }

    private void setTriggerInAutomation(boolean triggerInAuto) {
        m_triggerInAutomation = triggerInAuto;
    }

    private boolean isTriggerInAutomation() {
        return m_triggerInAutomation;
    }

    private void setTriggerStatement(Statement triggerStatement) {
        m_triggerStatement = triggerStatement;
    }

    private Statement getTriggerStatement() {
        return m_triggerStatement;
    }

    private void setTriggerResultSet(ResultSet triggerResultSet) {
        m_triggerResultSet = triggerResultSet;
    }

    private ResultSet getTriggerResultSet() {
        return m_triggerResultSet;
    }

    private void setActionColumns(Collection actionColumns) {
        m_actionColumns = actionColumns;
    }

    private Collection getActionColumns() {
        return m_actionColumns;
    }

    private void setConn(Connection conn) {
        m_conn = conn;
    }

    private Connection getConn() {
        return m_conn;
    }

    public void setReady(boolean ready) {
        m_ready = ready;
    }

    private void setTriggerSuccessful(boolean triggerSuccessful) {
        m_triggerSuccessful = triggerSuccessful;
    }

    private boolean isTriggerSuccessful() {
        return m_triggerSuccessful;
    }

}
