/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2005-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.vacuumd;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.opennms.core.logging.Logging;
import org.opennms.core.utils.PropertiesUtils;
import org.opennms.core.utils.PropertiesUtils.SymbolTable;
import org.opennms.netmgt.config.VacuumdConfigFactory;
import org.opennms.netmgt.config.vacuumd.Action;
import org.opennms.netmgt.config.vacuumd.ActionEvent;
import org.opennms.netmgt.config.vacuumd.Assignment;
import org.opennms.netmgt.config.vacuumd.AutoEvent;
import org.opennms.netmgt.config.vacuumd.Automation;
import org.opennms.netmgt.config.vacuumd.Trigger;
import org.opennms.netmgt.events.api.EventParameterUtils;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.scheduler.ReadyRunnable;
import org.opennms.netmgt.scheduler.Schedule;
import org.opennms.netmgt.xml.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class used to process automations configured in
 * the vacuumd-configuration.xml file.  Automations are
 * identified by a name and they reference
 * Triggers and Actions by name, as well.  Autmations also
 * have an interval attribute that determines how often
 * they run.
 *
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @version $Id: $
 */
public class AutomationProcessor implements ReadyRunnable {
	
	private static final Logger LOG = LoggerFactory.getLogger(AutomationProcessor.class);

    private final Automation m_automation;
    private final TriggerProcessor m_trigger;
    private final ActionProcessor m_action;
    
    /** 
     * @deprecated Associate {@link Automation} objects with {@link ActionEvent} instances instead.
     */
    private final AutoEventProcessor m_autoEvent;
    private final ActionEventProcessor m_actionEvent;
    
    private volatile Schedule m_schedule;
    private volatile boolean m_ready = false;

    static class TriggerProcessor {
    	private static final Logger LOG = LoggerFactory.getLogger(TriggerProcessor.class);

    	private final Trigger m_trigger;

        public TriggerProcessor(String automationName, Trigger trigger) {
            m_trigger = trigger;
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

		public String getName() {
			return getTrigger().getName();
		}
		
        @Override
		public String toString() {
			return m_trigger == null ? "<No-Trigger>" : m_trigger.getName();
		}

		ResultSet runTriggerQuery() throws SQLException {
			try {
				if (!hasTrigger()) {
					return null;
				}
                
                Connection conn = Transaction.getConnection(m_trigger.getDataSource());

                Statement triggerStatement = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
                Transaction.register(triggerStatement);

                ResultSet triggerResultSet = triggerStatement.executeQuery(getTriggerSQL());
                Transaction.register(triggerResultSet);

                return triggerResultSet;
			} catch (SQLException e) {
				LOG.warn("Error executing trigger {}", getName(), e);
				throw e;
			}
		}

		/**
		 * This method verifies that the number of rows in the result set of the trigger
		 * match the defined operation in the config.  For example, if the user has specified
		 * that the trigger-rows = 5 and the operator ">", the automation will only run
		 * if the result rows is greater than 5.
		 * @param trigRowCount
		 * @param trigOp
		 * @param resultRows
		 * @param processor TODO
		 */
		public boolean triggerRowCheck(int trigRowCount, String trigOp, int resultRows) {
		    
		    if (trigRowCount == 0 || trigOp == null) {
		        LOG.debug("triggerRowCheck: trigger has no row-count restrictions: operator is: {}, row-count is: {}", trigOp, trigRowCount);
		        return true;
		    }
		    
		    LOG.debug("triggerRowCheck: Verifying trigger resulting row count {} is {} {}", resultRows, trigOp, trigRowCount);
		    
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
		    
		    LOG.debug("Row count verification is: {}", runAction);
		    
		    return runAction;
		}
        
        
    }
    
    static class TriggerResults {
    	private final TriggerProcessor m_trigger;
    	private final ResultSet m_resultSet;
    	private final boolean m_successful;
    	
		public TriggerResults(TriggerProcessor trigger, ResultSet set, boolean successful) {
			m_trigger = trigger;
			m_resultSet = set;
			m_successful = successful;
		}
		
		public boolean hasTrigger() {
			return m_trigger.hasTrigger();
		}

        public ResultSet getResultSet() {
            return m_resultSet;
        }

        public boolean isSuccessful() {
            return m_successful;
        }
        
    }
    
    static class ActionProcessor {
    	private static final Logger LOG = LoggerFactory.getLogger(ActionProcessor.class);
        
        private final String m_automationName;
        private final Action m_action;

        public ActionProcessor(String automationName, Action action) {
            m_automationName = automationName;
            m_action = action;
        }
        
        public boolean hasAction() {
            return m_action != null;
        }

        public Action getAction() {
            return m_action;
        }

        String getActionSQL() {
            return getAction().getStatement().getContent();
        }

        PreparedStatement createPreparedStatement() throws SQLException {
            String actionJDBC = getActionSQL().replaceAll("\\$\\{\\w+\\}", "?");
            
            LOG.debug("createPrepareStatement: This action SQL: {}\nTurned into this: {}", getActionSQL(), actionJDBC);
            
            Connection conn = Transaction.getConnection(m_action.getDataSource());
            PreparedStatement stmt = conn.prepareStatement(actionJDBC);
            Transaction.register(stmt);
            return stmt;
        }

        /**
         * Returns an ArrayList containing the names of column defined
         * as tokens in the action statement defined in the config.  If no
         * tokens are found, an empty list is returned.
         * @param targetString
         * @return
         */
        public List<String> getActionColumns() {
        	return getTokenizedColumns(getActionSQL());
        }

        private List<String> getTokenizedColumns(String targetString) {
            // The \w represents a "word" charactor
            String expression = "\\$\\{(\\w+)\\}";
            Pattern pattern = Pattern.compile(expression);
            Matcher matcher = pattern.matcher(targetString);
            
            LOG.debug("getTokenizedColumns: processing string: {}", targetString);
            
            List<String> tokens = new ArrayList<>();
            int count = 0;
            while (matcher.find()) {
                count++;
                LOG.debug("getTokenizedColumns: Token {}: {}", count, matcher.group(1));
                
                tokens.add(matcher.group(1));
            }
            return tokens;        
        }
        
        void assignStatementParameters(PreparedStatement stmt, ResultSet rs) throws SQLException {
            List<String> actionColumns = getTokenizedColumns(getActionSQL());        
            Iterator<String> it = actionColumns.iterator();
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
            
            LOG.debug("getTokenCount: processing string: {}", targetString);
            
            int count = 0;
            while (matcher.find()) {
                count++;
                LOG.debug("getTokenCount: Token {}: {}", count, matcher.group(1));
            }
            return count;
        }

        boolean execute() throws SQLException {
            //No trigger defined, just running the action.
            if (getTokenCount(getActionSQL()) != 0) {
                LOG.info("execute: not running action: {}.  Action contains tokens in an automation ({}) with no trigger.", m_action.getName(), m_automationName);
                return false;
            } else {
                //Convert the sql to a PreparedStatement
                PreparedStatement actionStatement = createPreparedStatement();
                actionStatement.executeUpdate();
                return true;
            }
        }

        boolean processTriggerResults(TriggerResults triggerResults) throws SQLException {
        	ResultSet triggerResultSet = triggerResults.getResultSet();

            triggerResultSet.beforeFirst();
            
            PreparedStatement actionStatement = createPreparedStatement();
        
            //Loop through the select results
            while (triggerResultSet.next()) {                        
                //Convert the sql to a PreparedStatement
                assignStatementParameters(actionStatement, triggerResultSet);
                actionStatement.executeUpdate();
            }

            return true;
        }

		boolean processAction(TriggerResults triggerResults) throws SQLException {
			if (triggerResults.hasTrigger()) {
			    return processTriggerResults(triggerResults);
			} else {
			    return execute();
			}
		}

		public String getName() {
			return m_action.getName();
		}
		
        @Override
		public String toString() {
			return m_action.getName();
		}

        public void checkForRequiredColumns(TriggerResults triggerResults) {
        	ResultSet triggerResultSet = triggerResults.getResultSet();
        	if (!resultSetHasRequiredActionColumns(triggerResultSet, getActionColumns())) {
        		throw new AutomationException("Action "+this+" uses column not defined in trigger: "+triggerResults);
        	}
        }

        /**
         * Helper method that verifies tokens in a config defined action
         * are available in the ResultSet of the paired trigger
         * @param rs
         * @param actionColumns TODO
         * @param actionSQL
         * @param processor TODO
         * @return
         */
        public boolean resultSetHasRequiredActionColumns(ResultSet rs, Collection<String> actionColumns) {
            
            LOG.debug("resultSetHasRequiredActionColumns: Verifying required action columns in trigger ResultSet...");
            
            if (actionColumns.isEmpty()) {
            	return true;
            }
            
            if (rs == null) {
            	return false;
            }
            
            boolean verified = true;
            String actionColumnName = null;
            
            Iterator<String> it = actionColumns.iterator();
            
            while (it.hasNext()) {
                actionColumnName = (String)it.next();
                try {
                    if (rs.findColumn(actionColumnName) > 0) {
                    }
                } catch (SQLException e) {
                    LOG.warn("resultSetHasRequiredActionColumns: Trigger ResultSet does NOT have required action columns.  Missing: {}", actionColumnName);
                    LOG.warn(e.getMessage());
                    verified = false;
                }
            }
        
            return verified;
        }
        
    }
    
    /**
     * @deprecated Use {@link ActionEventProcessor} instead.
     */
    static class AutoEventProcessor {

    	private static final Logger LOG = LoggerFactory.getLogger(ActionProcessor.class);

    	private final String m_automationName;
        private final AutoEvent m_autoEvent;
        
        /**
         * @deprecated Use {@link ActionEventProcessor} instead.
         */
        public AutoEventProcessor(String automationName, AutoEvent autoEvent) {
            m_automationName = automationName;
            m_autoEvent = autoEvent;
        }
        
        public boolean hasEvent() {
            return m_autoEvent != null;
        }

        public AutoEvent getAutoEvent() {
            return m_autoEvent;
        }

        String getUei() {
            if (hasEvent()) {
                return getAutoEvent().getUei().getContent().orElse(null);
            }
            return null;
        }

        Event getEvent() {
            
            if (hasEvent()) {
                //create and send event
                LOG.debug("AutoEventProcessor: Generated auto-event {} for automation {}", getUei(), m_automationName);
                
                EventBuilder bldr = new EventBuilder(getUei(), "Automation");
                return bldr.getEvent();
            } else {
                LOG.debug("AutoEventProcessor: No auto-event for automation {}", m_automationName);
                return null;
            }
        }
    }
    
    static class SQLExceptionHolder extends RuntimeException {
    	private static final long serialVersionUID = 2479066089399740468L;

        private final SQLException m_ex;
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
        private final ResultSet m_rs;
        
        public ResultSetSymbolTable(ResultSet rs) {
            m_rs = rs;
        }

        @Override
        public String getSymbolValue(String symbol) {
            try {
                return m_rs.getString(symbol);
            } catch (SQLException e) {
                throw new SQLExceptionHolder(e);
            }
        }
        
    }
    
    static class InvalidSymbolTable implements PropertiesUtils.SymbolTable {

        @Override
        public String getSymbolValue(String symbol) {
            throw new IllegalArgumentException("token "+symbol+" is not allowed for "+this+" when no trigger is being processed");
        }
        
    }

    
    static class EventAssignment {
    	static final Pattern s_pattern = Pattern.compile("\\$\\{(\\w+)\\}");
        private final Assignment m_assignment;

        public EventAssignment(Assignment assignment) {
            m_assignment = assignment;
        }

        public void assign(EventBuilder bldr, PropertiesUtils.SymbolTable symbols) {
            
            String val = PropertiesUtils.substitute(m_assignment.getValue(), symbols);
            
            if (m_assignment.getValue().equals(val) && s_pattern.matcher(val).matches()) {
                // no substitution was made the value was a token pattern so skip it 
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

    	private static final Logger LOG = LoggerFactory.getLogger(ActionEventProcessor.class);

    	private final String m_automationName;
        private final ActionEvent m_actionEvent;
        private final List<EventAssignment> m_assignments;
        
        public ActionEventProcessor(String automationName, ActionEvent actionEvent) {
            m_automationName = automationName;
            m_actionEvent = actionEvent;
            
            if (actionEvent != null) {
                m_assignments = actionEvent.getAssignments().parallelStream().map(a -> {
                    return new EventAssignment(a);
                }).collect(Collectors.toList());
            } else {
                m_assignments = null;
            }
            
        }
        
        public boolean hasEvent() {
            return m_actionEvent != null;
        }

        public Event getEvent() {
            if (hasEvent()) {
                // the uei will be set by the event assignments
                EventBuilder bldr = new EventBuilder(null, "Automation");
                buildEvent(bldr, new InvalidSymbolTable());
                LOG.debug("ActionEventProcessor: Generated action-event {} for automation {}", bldr.getEvent().getUei(), m_automationName);
                return bldr.getEvent();
            } else {
                LOG.debug("ActionEventProcessor: No action-event for automation {}", m_automationName);
                return null;
            }
        }

        private void buildEvent(EventBuilder bldr, SymbolTable symbols) {
            for(EventAssignment assignment : m_assignments) {
                assignment.assign(bldr, symbols);
            }
        }

        List<Event> processTriggerResults(TriggerResults triggerResults) throws SQLException {
            if (!hasEvent()) {
                LOG.debug("processTriggerResults: No action-event for automation {}", m_automationName);
                return Collections.emptyList();
            }
            
            ResultSet triggerResultSet = triggerResults.getResultSet();
            
            triggerResultSet.beforeFirst();
            
            //Loop through the select results
            List<Event> events = new LinkedList<>();
            while (triggerResultSet.next()) {
                // the uei will be set by the event assignments
                EventBuilder bldr = new EventBuilder(null, "Automation");
                ResultSetSymbolTable symbols = new ResultSetSymbolTable(triggerResultSet);
                
                try {
                    buildEvent(bldr, symbols);
                } catch (SQLExceptionHolder holder) {
                    holder.rethrow();
                }
                LOG.debug("processTriggerResults: Generated action-event {} for automation {}", bldr.getEvent().getUei(), m_automationName);
                events.add(bldr.getEvent());
            }
            return events;
        }

        private boolean resultHasColumn(ResultSet resultSet, String columnName) {
            try {
                if (resultSet.findColumn(columnName) > 0) {
                    return true;
                }
            } catch (SQLException e) {
            }
            return false;
        }

        public boolean forEachResult() {
            return m_actionEvent == null ? false : m_actionEvent.getForEachResult();
        }

        /**
         * Generates the list of events that should be sent once the transation is closed.
         */
        List<Event> processActionEvent(TriggerResults triggerResults) throws SQLException {
            if (triggerResults.hasTrigger() && forEachResult()) {
                return processTriggerResults(triggerResults);
            } else if (hasEvent()) {
                return Collections.singletonList(getEvent());
            }
            return Collections.emptyList();
        }
    }

    /**
     * Public constructor.
     *
     * @param automation a {@link org.opennms.netmgt.config.vacuumd.Automation} object.
     */
    @SuppressWarnings("deprecation")
	public AutomationProcessor(Automation automation) {
        m_ready = true;
        m_automation = automation;
        m_trigger = new TriggerProcessor(m_automation.getName(), VacuumdConfigFactory.getInstance().getTrigger(m_automation.getTriggerName().orElse(null)));
        m_action = new ActionProcessor(m_automation.getName(), VacuumdConfigFactory.getInstance().getAction(m_automation.getActionName()));
        m_autoEvent = new AutoEventProcessor(m_automation.getName(), VacuumdConfigFactory.getInstance().getAutoEvent(m_automation.getAutoEventName().orElse(null)));
        m_actionEvent = new ActionEventProcessor(m_automation.getName(),VacuumdConfigFactory.getInstance().getActionEvent(m_automation.getActionEvent().orElse(null)));
    }
    
    /**
     * <p>getAction</p>
     *
     * @return a {@link org.opennms.netmgt.vacuumd.AutomationProcessor.ActionProcessor} object.
     */
    public ActionProcessor getAction() {
        return m_action;
    }
    
    /**
     * <p>getTrigger</p>
     *
     * @return a {@link org.opennms.netmgt.vacuumd.AutomationProcessor.TriggerProcessor} object.
     */
    public TriggerProcessor getTrigger() {
        return m_trigger;
    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    /**
     * <p>run</p>
     */
    @Override
    public void run() {
        final Map<String,String> mdc = Logging.getCopyOfContextMap();
        Logging.putPrefix("vacuumd");

        Date startDate = new Date();
        LOG.debug("Start Scheduled automation {}", this);
        
        if (getAutomation() != null) {
            setReady(false);
            try {
                runAutomation();
            } catch (SQLException e) {
                LOG.warn("Error running automation: {}, {}", getAutomation().getName(), e.getMessage());
            } finally {
                setReady(true);
            }
        }

        LOG.debug("run: Finished automation {}, started at {}", m_automation.getName(), startDate);
        Logging.setContextMap(mdc);
    }

    /**
     * Called by the run method to execute the sql statements
     * of triggers and actions defined for an automation.  An
     * automation may have 0 or 1 trigger and must have 1 action.
     * If the automation doesn't have a trigger than the action
     * must not contain any tokens.
     *
     * @throws java.sql.SQLException if any.
     * @return a boolean.
     */
    public boolean runAutomation() throws SQLException {
        LOG.debug("runAutomation: {} running...", m_automation.getName());

        if (hasTrigger()) {
            LOG.debug("runAutomation: {} trigger statement is: {}", m_automation.getName(), m_trigger.getTriggerSQL());
        }
            
        LOG.debug("runAutomation: {} action statement is: {}", m_automation.getName(), m_action.getActionSQL());

        LOG.debug("runAutomation: Executing trigger: {}", m_automation.getTriggerName().orElse(null));

        final List<Event> eventsToSend = new LinkedList<>();
        Transaction.begin();
        try {
            LOG.debug("runAutomation: Processing automation: {}", m_automation.getName());

            TriggerResults results = processTrigger();
            
            boolean success = false;
            if (results.isSuccessful()) {
                success = processAction(results, eventsToSend);
            }
            
			return success;

        } catch (Throwable e) {
        	Transaction.rollbackOnly();
            LOG.warn("runAutomation: Could not execute automation: {}", m_automation.getName(), e);
            return false;
        } finally {
            LOG.debug("runAutomation: Closing transaction for automation: {}", m_automation.getName());
            Transaction.end();

            // Always send the events out after the transaction is closed in order to ensure
            // that any event handlers can access the updated records
            LOG.debug("runAutomation: Sending {} events for automation: {}", eventsToSend.size(), m_automation.getName());
            for (Event event : eventsToSend) {
                Vacuumd.getSingleton().getEventManager().sendNow(event);
            }

            LOG.debug("runAutomation: Done processing automation: {}", m_automation.getName());
        }

    }

    private boolean processAction(TriggerResults triggerResults, List<Event> eventsToSend) throws SQLException {
		LOG.debug("runAutomation: running action(s)/actionEvent(s) for : {}", m_automation.getName());
		
        //Verfiy the trigger ResultSet returned the required number of rows and the required columns for the action statement
        m_action.checkForRequiredColumns(triggerResults);
        		
		if (m_action.processAction(triggerResults)) {
		    eventsToSend.addAll(m_actionEvent.processActionEvent(triggerResults));
		    if (m_autoEvent.hasEvent()) {
		        eventsToSend.add(m_autoEvent.getEvent());
		    }
		    return true;
		} else {
			return false;
		}
	}

	private TriggerResults processTrigger() throws SQLException {
		
		if (m_trigger.hasTrigger()) {
			//get a scrollable ResultSet so that we can count the rows and move back to the
            //beginning for processing.
			
            ResultSet triggerResultSet = m_trigger.runTriggerQuery();

            TriggerResults triggerResults = new TriggerResults(m_trigger, triggerResultSet, verifyRowCount(triggerResultSet));

			return triggerResults;
            
        } else {
            return new TriggerResults(m_trigger, null, true);
        }
	}

	/**
	 * <p>verifyRowCount</p>
	 *
	 * @param triggerResultSet a {@link java.sql.ResultSet} object.
	 * @return a boolean.
	 * @throws java.sql.SQLException if any.
	 */
	protected boolean verifyRowCount(ResultSet triggerResultSet) throws SQLException {
        if (!m_trigger.hasTrigger()) {
            return true;
        }
        
        
        int resultRows;
        boolean validRows = true;
        //determine if number of rows required by the trigger row-count and operator were
        //met by the trigger query, if so we'll run the action
        resultRows = countRows(triggerResultSet);
        
        int triggerRowCount = m_trigger.getTrigger().getRowCount();
        String triggerOperator = m_trigger.getTrigger().getOperator();

        LOG.debug("verifyRowCount: Verifying trigger result: {} is {} than {}", resultRows, (triggerOperator == null ? "<null>" : triggerOperator), triggerRowCount);

        if (!m_trigger.triggerRowCheck(triggerRowCount, triggerOperator, resultRows))
            validRows = false;

        return validRows;
    }

    /**
     * Method used to count the rows in a ResultSet.  This probably requires
     * that your ResultSet is scrollable.
     *
     * @param rs a {@link java.sql.ResultSet} object.
     * @throws java.sql.SQLException if any.
     * @return a int.
     */
    public int countRows(ResultSet rs) throws SQLException {
        if (rs == null) {
            return 0;
        }

        int rows = 0;
        while (rs.next())
            rows++;
        rs.beforeFirst();
        return rows;
    }

    /**
     * Simple helper method to determine if the targetString contains
     * any '${token}'s.
     *
     * @param targetString a {@link java.lang.String} object.
     * @return a boolean.
     */
    public boolean containsTokens(String targetString) {
        return m_action.getTokenCount(targetString) > 0;
    }

    /**
     * <p>getAutomation</p>
     *
     * @return Returns the automation.
     */
    public Automation getAutomation() {
        return m_automation;
    }
    
    /**
     * <p>isReady</p>
     *
     * @return a boolean.
     */
    @Override
    public boolean isReady() {
        return m_ready;
    }

    /**
     * <p>getSchedule</p>
     *
     * @return Returns the schedule.
     */
    public Schedule getSchedule() {
        return m_schedule;
    }
    

    /**
     * <p>setSchedule</p>
     *
     * @param schedule The schedule to set.
     */
    public void setSchedule(Schedule schedule) {
        m_schedule = schedule;
    }
    
    private boolean hasTrigger() {
        return m_trigger.hasTrigger();
    }

    /**
     * <p>setReady</p>
     *
     * @param ready a boolean.
     */
    public void setReady(boolean ready) {
        m_ready = ready;
    }

}
