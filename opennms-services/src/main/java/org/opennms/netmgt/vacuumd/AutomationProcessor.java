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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Category;
import org.opennms.core.utils.PropertiesUtils;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.core.utils.PropertiesUtils.SymbolTable;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.DataSourceFactory;
import org.opennms.netmgt.config.VacuumdConfigFactory;
import org.opennms.netmgt.config.vacuumd.Action;
import org.opennms.netmgt.config.vacuumd.ActionEvent;
import org.opennms.netmgt.config.vacuumd.Assignment;
import org.opennms.netmgt.config.vacuumd.AutoEvent;
import org.opennms.netmgt.config.vacuumd.Automation;
import org.opennms.netmgt.config.vacuumd.Trigger;
import org.opennms.netmgt.scheduler.ReadyRunnable;
import org.opennms.netmgt.scheduler.Schedule;
import org.opennms.netmgt.utils.EventBuilder;
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

    private boolean m_triggerInAutomation;
    private Statement m_triggerStatement;
    private ResultSet m_triggerResultSet;
    private Collection m_actionColumns;
    private Connection m_conn;
    
    private TriggerProcessor m_trigger;
    private ActionProcessor m_action;
    private AutoEventProcessor m_autoEvent;
    private ActionEventProcessor m_actionEvent;
    
    static class TriggerProcessor {
        private String m_automationName;
        private Trigger m_trigger;
        private boolean m_successful; 

        public boolean isSuccessful() {
            return m_successful;
        }

        public void setSuccessful(boolean successful) {
            m_successful = successful;
        }

        public TriggerProcessor(String automationName, Trigger trigger) {
            m_automationName = automationName;
            m_trigger = trigger;
        }

        public Category log() {
            return ThreadCategory.getInstance(getClass());
        }
        
        public Trigger getTrigger() {
            return m_trigger;
        }
        
        public boolean hasTrigger() {
            return m_trigger != null;
        }

        public String getTriggerSQL() {
            if (hasTrigger()) {
                return getTrigger().getStatement().getContent();
            } else {
                return null;
            }
        }
        
        
    }
    
    static class ActionProcessor {
        
        private String m_automationName;
        private Action m_action;
        private boolean m_successful;

        public ActionProcessor(String automationName,Action action) {
            m_automationName = automationName;
            m_action = action;
        }
        
        public boolean hasAction() {
            return m_action != null;
        }

        public Action getAction() {
            return m_action;
        }

        public Category log() {
            return ThreadCategory.getInstance(getClass());
        }
        
        String getActionSQL() {
            return getAction().getStatement().getContent();
        }

        PreparedStatement createPreparedStatement(Connection conn) throws SQLException {
            String actionJDBC = getActionSQL().replaceAll("\\$\\{\\w+\\}", "?");
            
            log().debug("createPrepareStatement: This action SQL: "+getActionSQL()+"\nTurned into this: "+actionJDBC);
            
            return conn.prepareStatement(actionJDBC);
        }

        /**
         * Returns an ArrayList containing the names of column defined
         * as tokens in the action statement defined in the config.  If no
         * tokens are found, an empty list is returned.
         * @param targetString
         * @return
         */
        public List<String> getTokenizedColumns(String targetString) {
            // The \w represents a "word" charactor
            String expression = "\\$\\{(\\w+)\\}";
            Pattern pattern = Pattern.compile(expression);
            Matcher matcher = pattern.matcher(targetString);
            
            log().debug("getTokenizedColumns: processing string: "+targetString);
            
            List<String> tokens = new ArrayList<String>();
            int count = 0;
            while (matcher.find()) {
                count++;
                log().debug("getTokenizedColumns: Token "+count+": "+matcher.group(1));
                
                tokens.add(matcher.group(1));
            }
            return tokens;        
        }

        void assignStatementParameters(PreparedStatement stmt, ResultSet rs) throws SQLException {
            List<String> actionColumns = getTokenizedColumns(getActionSQL());        
            Iterator it = actionColumns.iterator();
            String actionColumnName = null;
            int i=0;
            while (it.hasNext()) {
                actionColumnName = (String)it.next();
                stmt.setObject(++i, rs.getObject(actionColumnName));
            }
        
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

        public boolean isSuccessful() {
            return m_successful;
        }

        public void setSuccessful(boolean successful) {
            m_successful = successful;
        }

        private void execute(Connection conn) throws SQLException {
            setSuccessful(false);
            //No trigger defined, just running the action.
            if (getTokenCount(getActionSQL()) != 0) {
                log().info("execute: not running action: "+m_action.getName()+".  Action contains tokens in an automation ("+m_automationName+") with no trigger.");
                setSuccessful(false);
            } else {
                //Convert the sql to a PreparedStatement
                PreparedStatement actionStatement = createPreparedStatement(conn);
                actionStatement.executeUpdate();
                setSuccessful(true);
            }
        }

        void processTriggerResults(Connection conn, ResultSet triggerResultSet) throws SQLException {
            setSuccessful(false);
            triggerResultSet.beforeFirst();
            
            PreparedStatement actionStatement = createPreparedStatement(conn);
        
            //Loop through the select results
            while (triggerResultSet.next()) {                        
                //Convert the sql to a PreparedStatement
                assignStatementParameters(actionStatement, triggerResultSet);
                actionStatement.executeUpdate();
                /*
                 * TODO: create new XSD configuration to allow a way to send an event for each action per row.
                 * Currently, the XSD presents itself as one event per automation.
                 */
            }
            setSuccessful(true);
        }
        
    }
    
    static class AutoEventProcessor {

        private AutoEvent m_autoEvent;
        private String m_automationName;
        
        public AutoEventProcessor(String automationName, AutoEvent autoEvent) {
            m_automationName = automationName;
            m_autoEvent = autoEvent;
        }
        
        public Category log() {
            return ThreadCategory.getInstance(getClass());
        }
        
        public boolean hasEvent() {
            return m_autoEvent != null;
        }

        public AutoEvent getAutoEvent() {
            return m_autoEvent;
        }

        String getUei() {
            if (hasEvent()) {
                return getAutoEvent().getUei().getContent();
            } else {
                return null;
            }
        }

        void send() {
            log().debug("runAutomation: Sending any possible configured event for automation: "+m_automationName);
            
            if (hasEvent()) {
                //create and send event
                log().debug("runAutomation: Sending event: "+getUei()+" for automation: "+m_automationName);
                
                EventBuilder bldr = new EventBuilder(getUei(), "Automation");
                sendEvent(bldr.getEvent());
            } else {
                log().debug("runAutomation: No event configured automation: "+m_automationName);             
            }
        }

        private void sendEvent(Event event) {
            Vacuumd.getSingleton().getEventManager().sendNow(event);
        }

    }
    
    static class SQLExceptionHolder extends RuntimeException {
        SQLException m_ex = null;
        public SQLExceptionHolder(SQLException ex) {
            m_ex = ex;
        }
        
        public void rethrow() throws SQLException {
            if (m_ex != null) {
                throw m_ex;
            }
        }
    }

    static class ResultSetSymbolTable implements PropertiesUtils.SymbolTable {
        private ResultSet m_rs;
        
        public ResultSetSymbolTable(ResultSet rs) {
            m_rs = rs;
        }

        public String getSymbolValue(String symbol) {
            try {
                return m_rs.getString(symbol);
            } catch (SQLException e) {
                throw new SQLExceptionHolder(e);
            }
        }
        
    }
    
    static class InvalidSymbolTable implements PropertiesUtils.SymbolTable {

        public String getSymbolValue(String symbol) {
            throw new IllegalArgumentException("token "+symbol+" is not allowed for "+this+" when no trigger is being processed");
        }
        
    }

    
    static class EventAssignment {

        static final Pattern s_pattern = Pattern.compile("\\$\\{(\\w+)\\}");
        private Assignment m_assignment;

        public EventAssignment(Assignment assignment) {
            m_assignment = assignment;
        }

        public Category log() {
            return ThreadCategory.getInstance(getClass());
        }
        
        public void assign(EventBuilder bldr, PropertiesUtils.SymbolTable symbols) {

            String val = PropertiesUtils.substitute(m_assignment.getValue(), symbols);
            
            if (m_assignment.getValue().equals(val) && s_pattern.matcher(val).matches()) {
                // no substituion was made the value was a token pattern so skip it 
                return;
            }

            if ("field".equals(m_assignment.getType())) {
                bldr.setField(m_assignment.getName(), val);
            } else {
                bldr.addParam(m_assignment.getName(), val);
            }
        }
        
    }

    static class ActionEventProcessor {

        private ActionEvent m_actionEvent;
        private String m_automationName;
        private List<EventAssignment> m_assignments;
        
        public ActionEventProcessor(String automationName, ActionEvent actionEvent) {
            m_automationName = automationName;
            m_actionEvent = actionEvent;
            
            if (actionEvent != null) {
        
                m_assignments = new ArrayList<EventAssignment>(actionEvent.getAssignmentCount());
                for(Assignment assignment : actionEvent.getAssignment()) {
                    m_assignments.add(new EventAssignment(assignment));
                }
            
            }
            
        }
        
        public Category log() {
            return ThreadCategory.getInstance(getClass());
        }
        
        public boolean hasEvent() {
            return m_actionEvent != null;
        }

        void send() {
            log().debug("runAutomation: Sending any possible configured event for automation: "+m_automationName);
            
            if (hasEvent()) {
                EventBuilder bldr = new EventBuilder(new Event());
                buildEvent(bldr, new InvalidSymbolTable());
                sendEvent(bldr.getEvent());
                
            } else {
                log().debug("runAutomation: No event configured automation: "+m_automationName);             
            }
        }

        private void buildEvent(EventBuilder bldr, SymbolTable symbols) {
            for(EventAssignment assignment : m_assignments) {
                assignment.assign(bldr, symbols);
            }
        }

        private void sendEvent(Event event) {
            Vacuumd.getSingleton().getEventManager().sendNow(event);
        }

        void processTriggerResults(ResultSet triggerResultSet) throws SQLException {
            if (!hasEvent()) return;
            
            triggerResultSet.beforeFirst();
            
            //Loop through the select results
            while (triggerResultSet.next()) {  
                EventBuilder bldr = new EventBuilder(new Event());
                bldr.setSource("Automation");
                ResultSetSymbolTable symbols = new ResultSetSymbolTable(triggerResultSet);
                
                try {
                    buildEvent(bldr, symbols);
                } catch (SQLExceptionHolder holder) {
                    holder.rethrow();
                }

                sendEvent(bldr.getEvent());
            }

        }

        public boolean forEachResult() {
            return m_actionEvent == null ? false : m_actionEvent.getForEachResult();
        }
        
    }

    /**
     * Public constructor.
     *
     */
    public AutomationProcessor(Automation automation) {
        m_ready = true;
        m_automation = automation;
        m_trigger = new TriggerProcessor(m_automation.getName(), VacuumdConfigFactory.getInstance().getTrigger(m_automation.getTriggerName()));
        m_action = new ActionProcessor(m_automation.getName(), VacuumdConfigFactory.getInstance().getAction(m_automation.getActionName()));
        m_autoEvent = new AutoEventProcessor(m_automation.getName(), VacuumdConfigFactory.getInstance().getAutoEvent(m_automation.getAutoEventName()));
        m_actionEvent = new ActionEventProcessor(m_automation.getName(),VacuumdConfigFactory.getInstance().getActionEvent(m_automation.getActionEvent()));
    }
    
    public ActionProcessor getAction() {
        return m_action;
    }
    
    public TriggerProcessor getTrigger() {
        return m_trigger;
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

        setTriggerInAutomation(m_trigger.hasTrigger());

        if (isTriggerInAutomation()) {
            log().debug("runAutomation: "+m_automation.getName()+" trigger statement is: "+ m_trigger.getTriggerSQL());
        }
            
        log().debug("runAutomation: "+m_automation.getName()+" action statement is: "+m_action.getActionSQL());

        setFields();

        log().debug("runAutomation: Executing trigger: "+m_automation.getTriggerName());

        try {
            setConn(DataSourceFactory.getInstance().getConnection());

            processTrigger();
            
            if (!m_trigger.isSuccessful()) {
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

        return m_action.isSuccessful();
    }

    private void processAction() throws SQLException {
        try {
            log().debug("runAutomation: running action(s): "+m_automation.getActionName());
            
            getConn().setAutoCommit(false);
            if (isTriggerInAutomation()) {
                m_action.processTriggerResults(getConn(), getTriggerResultSet());
            } else {
                m_action.execute(getConn());
            }
            if (m_action.isSuccessful()) {
                if (isTriggerInAutomation() && m_actionEvent.forEachResult()) {
                    m_actionEvent.processTriggerResults(getTriggerResultSet());
                } else {
                    m_actionEvent.send();
                }
            }
            if (m_action.isSuccessful()) {
                m_autoEvent.send();
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
            setTriggerResultSet(getTriggerStatement().executeQuery(m_trigger.getTriggerSQL()));

            //Verfiy the trigger ResultSet returned the required number of rows and the required columns for the action statement
            if (!verifyRowCount(getTriggerResultSet()) || !resultSetHasRequiredActionColumns(getTriggerResultSet(), getActionColumns())) {
                m_trigger.setSuccessful(false);
            } else {
                m_trigger.setSuccessful(true);
            }
        } else {
            m_trigger.setSuccessful(true);
        }
    }

    private void setFields() {
        setActionColumns(m_action.getTokenizedColumns(m_action.getActionSQL()));
        setConn(null);
        setTriggerStatement(null);
        setTriggerResultSet(null);
    }

    protected boolean verifyRowCount(ResultSet triggerResultSet) throws SQLException {
        int resultRows;
        boolean validRows = true;
        //determine if number of rows required by the trigger row-count and operator were
        //met by the trigger query, if so we'll run the action
        resultRows = countRows(triggerResultSet);
        
        int triggerRowCount = m_trigger.getTrigger().getRowCount();
        String triggerOperator = m_trigger.getTrigger().getOperator();

        log().debug("verifyRowCount: Verifying trigger result: "+resultRows+" is "+triggerOperator+" than "+triggerRowCount);

        if (!triggerRowCheck(triggerRowCount, triggerOperator, resultRows))
            validRows = false;

        return validRows;
    }

    /**
     * Helper method that verifies tokens in a config defined action
     * are available in the ResultSet of the paired trigger
     * @param rs
     * @param actionSQL
     * @return
     */
    public boolean resultSetHasRequiredActionColumns(ResultSet rs, String actionSQL) {
        Collection actionColumns = m_action.getTokenizedColumns(actionSQL);
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
     * Simple helper method to determine if the targetString contains
     * any '${token}'s.
     * @param targetString
     * @return
     */
    public boolean containsTokens(String targetString) {
        return m_action.getTokenCount(targetString) > 0;
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

}
