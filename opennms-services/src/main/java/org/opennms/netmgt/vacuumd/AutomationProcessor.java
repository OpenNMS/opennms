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
import org.opennms.netmgt.config.DataSourceFactory;
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
    public AutomationProcessor(Automation automation) {
        m_ready = true;
        m_automation = automation;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run() {

        Date startDate = new Date();
        log().debug("Start Scheduled automation "+this);
        
        if (getAutomation() != null) {
            setReady(false);
            try {
                runAutomation();
            } catch (SQLException e) {
                log().warn("Error running automation: "+getAutomation().getName()+", "+e.getMessage());
            } finally {
                setReady(true);
            }
        }

        log().debug("run: Finished automation "+m_automation.getName()+", started at "+startDate);
        
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
            log().debug("triggerRowCheck: trigger has no row-count restrictions: operator is: "+trigOp+", row-count is: "+trigRowCount);
            return true;
        }
        
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
        
        log().debug("Row count verification is: "+runAction);
        
        return runAction;
    }

    /**
     * Called by the run method to execute the sql statements
     * of triggers and actions defined for an automation.  An
     * automation may have 0 or 1 trigger and must have 1 action.
     * If the automation doesn't have a trigger than the action
     * must not contain any tokens. 
     * @param auto
     * 
     * @return
     * @throws SQLException
     */
    public boolean runAutomation() throws SQLException {
        log().debug("runAutomation: "+m_automation.getName()+" running...");

        setTriggerInAutomation(hasTrigger());

        if (isTriggerInAutomation()) {
            log().debug("runAutomation: "+m_automation.getName()+" trigger statement is: "+ getTriggerSQL());
        }
            
        log().debug("runAutomation: "+m_automation.getName()+" action statement is: "+getActionSQL());

        setFields();

        log().debug("runAutomation: Executing trigger: "+m_automation.getTriggerName());

        try {
            setConn(DataSourceFactory.getInstance().getConnection());

            processTrigger();
            
            if (!isTriggerSuccessful()) {
                return false;
            }


            processAction();

        } catch (SQLException e) {
            log().warn("runAutomation: Could not execute trigger: "+m_automation.getTriggerName(), e);
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

    private void processAction() throws SQLException {
        try {
            setActionSuccessful(false);
            
            log().debug("runAutomation: running action(s): "+m_automation.getActionName());
            
            getConn().setAutoCommit(false);
            if (isTriggerInAutomation()) {
                getTriggerResultSet().beforeFirst();
                
                //Loop through the select results
                while (getTriggerResultSet().next()) {                        
                    processActionStatement(getActionSQL(), getTriggerResultSet());
                    /*
                     * TODO: create new XSD configuration to allow a way to send an event for each action per row.
                     * Currently, the XSD presents itself as one event per automation.
                     */
                }
                setActionSuccessful(true);
                sendAutoEvent();
            } else {
                //No trigger defined, just running the action.
                if (getTokenCount(getActionSQL()) != 0) {
                    log().info("runAutomation: not running action: "+m_automation.getActionName()+".  Action contains tokens in an automation ("+m_automation.getName()+") with no trigger.");
                    setActionSuccessful(false);
                } else {
                    processActionStatement(getActionSQL(), getTriggerResultSet());
                    sendAutoEvent();
                    setActionSuccessful(true);
                }
            }
            getConn().commit();           
        } catch (SQLException e) {
            getConn().rollback();
            log().warn("runAutomation: Could not execute update on action: "+m_automation.getActionName());
            log().warn(e.getMessage());
        }
    }

    private void processTrigger() throws SQLException {
        if (isTriggerInAutomation()) {
            //get a scrollable ResultSet so that we can count the rows and move back to the
            //beginning for processing.
            setTriggerStatement(getConn().createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY));
            setTriggerResultSet(getTriggerStatement().executeQuery(getTriggerSQL()));

            //Verfiy the trigger ResultSet returned the required number of rows and the required columns for the action statement
            if (!verifyRowCount(getTriggerResultSet()) || !resultSetHasRequiredActionColumns(getTriggerResultSet(), getActionColumns())) {
                setTriggerSuccessful(false);
            } else {
                setTriggerSuccessful(true);
            }
        } else {
            setTriggerSuccessful(true);
        }
    }

    private void setFields() {
        setActionColumns(getTokenizedColumns(getActionSQL()));
        setConn(null);
        setTriggerStatement(null);
        setTriggerResultSet(null);
    }

    private void sendAutoEvent() {
        log().debug("runAutomation: Sending any possible configured event for automation: "+m_automation.getName());
        
        if (hasEvent()) {
            String uei = getUei();
            //create and send event
            log().debug("runAutomation: Sending event: "+uei+" for automation: "+m_automation.getName());
            
            Event e = createEvent("Automation", uei);
            Vacuumd.getSingleton().getEventManager().sendNow(e);
        } else {
            log().debug("runAutomation: No event configured automation: "+m_automation.getName());             
        }
    }

    private String getUei() {
        if (hasEvent()) {
            return VacuumdConfigFactory.getInstance().getAutoEvent(m_automation.getAutoEventName()).getUei().getContent();
        } else {
            return null;
        }
    }

    private Event createEvent(String source, String uei) {
        Event event = new Event();
        event.setSource(source);
        event.setUei(uei);
        String eventTime = EventConstants.formatToString(new Date());
        event.setCreationTime(eventTime);
        event.setTime(eventTime);
        return event;
    }

    protected boolean verifyRowCount(ResultSet triggerResultSet) throws SQLException {
        int resultRows;
        boolean validRows = true;
        //determine if number of rows required by the trigger row-count and operator were
        //met by the trigger query, if so we'll run the action
        resultRows = countRows(triggerResultSet);
        
        int triggerRowCount = VacuumdConfigFactory.getInstance().getTrigger(m_automation.getTriggerName()).getRowCount();
        String triggerOperator = VacuumdConfigFactory.getInstance().getTrigger(m_automation.getTriggerName()).getOperator();

        log().debug("verifyRowCount: Verifying trigger result: "+resultRows+" is "+triggerOperator+" than "+triggerRowCount);

        if (!triggerRowCheck(triggerRowCount, triggerOperator, resultRows))
            validRows = false;

        return validRows;
    }

    private void processActionStatement(String actionSQL, ResultSet triggerResultSet) throws SQLException {
        PreparedStatement actionStatement;
        //Convert the sql to a PreparedStatement
        actionStatement = convertActionToPreparedStatement(triggerResultSet, actionSQL);
        actionStatement.executeUpdate();
    }

    private String getActionSQL() {
        return VacuumdConfigFactory.getInstance().getAction(m_automation.getActionName()).getStatement().getContent();
    }

    public String getTriggerSQL() {
        if (hasTrigger()) {
            return VacuumdConfigFactory.getInstance().getTrigger(m_automation.getTriggerName()).getStatement().getContent();
        } else {
            return null;
        }
    }

    private boolean hasTrigger() {
        return m_automation.getTriggerName() != null;
    }
    
    private boolean hasEvent() {
        return m_automation.getAutoEventName() != null;
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
    public Collection<String> getTokenizedColumns(String targetString) {
        // The \w represents a "word" charactor
        String expression = "\\$\\{(\\w+)\\}";
        Pattern pattern = Pattern.compile(expression);
        Matcher matcher = pattern.matcher(targetString);
        
        log().debug("getTokenizedColumns: processing string: "+targetString);
        
        Collection<String> tokens = new ArrayList<String>();
        int count = 0;
        while (matcher.find()) {
            count++;
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
        
        log().debug("getTokenCount: processing string: "+targetString);
        
        int count = 0;
        while (matcher.find()) {
            count++;
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
    
    private Category log() {
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
