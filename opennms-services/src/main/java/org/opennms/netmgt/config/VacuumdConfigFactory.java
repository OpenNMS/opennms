/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.xml.CastorUtils;
import org.opennms.netmgt.config.vacuumd.Action;
import org.opennms.netmgt.config.vacuumd.ActionEvent;
import org.opennms.netmgt.config.vacuumd.AutoEvent;
import org.opennms.netmgt.config.vacuumd.Automation;
import org.opennms.netmgt.config.vacuumd.Statement;
import org.opennms.netmgt.config.vacuumd.Trigger;
import org.opennms.netmgt.config.vacuumd.VacuumdConfiguration;
import org.springframework.util.Assert;


/**
 * This is the singleton class used to load the configuration for the OpenNMS
 * Vacuumd process from the vacuumd-configuration xml file.
 *
 * <strong>Note: </strong>Users of this class should make sure the
 * <em>setReader()</em> method is called before calling any other method to ensure the
 * config is loaded before accessing other convenience methods.
 *
 * @author <a href="mailto:david@opennms.com">David Hustace </a>
 * @author <a href="mailto:brozow@opennms.com">Mathew Brozowski </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 * @author <a href="mailto:david@opennms.com">David Hustace </a>
 * @author <a href="mailto:brozow@opennms.com">Mathew Brozowski </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 * @author <a href="mailto:david@opennms.com">David Hustace </a>
 * @author <a href="mailto:brozow@opennms.com">Mathew Brozowski </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 * @version $Id: $
 */
public final class VacuumdConfigFactory {
    /**
     * The singleton instance of this factory
     */
    private static VacuumdConfigFactory m_singleton = null;

    private static boolean m_loadedFromFile = false;

    /**
     * The config class loaded from the config file
     */
    private VacuumdConfiguration m_config;

    /**
     * <p>Constructor for VacuumdConfigFactory.</p>
     *
     * @param stream a {@link java.io.InputStream} object.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public VacuumdConfigFactory(InputStream stream) throws MarshalException, ValidationException {
        m_config = CastorUtils.unmarshal(VacuumdConfiguration.class, stream);
    }

    /**
     * Load the config from the default config file and create the singleton
     * instance of this factory.
     *
     * @exception java.io.IOException
     *                Thrown if the specified config file cannot be read
     * @exception org.exolab.castor.xml.MarshalException
     *                Thrown if the file does not conform to the schema.
     * @exception org.exolab.castor.xml.ValidationException
     *                Thrown if the contents do not match the required schema.
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public static synchronized void init() throws IOException, MarshalException, ValidationException {
        if (m_singleton != null) {
            /*
             * The init method has already called, so return.
             * To reload, reload() will need to be called.
             */
            return;
        }

        InputStream is = null;

        try {
            is = new FileInputStream(ConfigFileConstants.getFile(ConfigFileConstants.VACUUMD_CONFIG_FILE_NAME));
            setInstance(new VacuumdConfigFactory(is));
        } finally {
            if (is != null) {
                IOUtils.closeQuietly(is);
            }
        }
        
        m_loadedFromFile = true;
    }

    /**
     * Reload the config from the default config file
     *
     * @exception java.io.IOException
     *                Thrown if the specified config file cannot be read/loaded
     * @exception org.exolab.castor.xml.MarshalException
     *                Thrown if the file does not conform to the schema.
     * @exception org.exolab.castor.xml.ValidationException
     *                Thrown if the contents do not match the required schema.
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public static synchronized void reload() throws IOException, MarshalException, ValidationException {
        if (m_loadedFromFile) {
            setInstance(null);

            init();
        }
    }

    /**
     * Return the singleton instance of this factory.
     *
     * @return The current factory instance.
     * @throws java.lang.IllegalStateException
     *             Thrown if the factory has not yet been initialized.
     */
    public static synchronized VacuumdConfigFactory getInstance() {
        Assert.state(m_singleton != null, "The factory has not been initialized");

        return m_singleton;
    }
    
    /**
     * Set the singleton instance of this factory.
     *
     * @param instance The factory instance to set.
     */
    public static synchronized void setInstance(VacuumdConfigFactory instance) {
        m_singleton = instance;
    }
    
    /**
     * Returns a Collection of automations defined in the config
     *
     * @return a {@link java.util.Collection} object.
     */
    public synchronized Collection<Automation> getAutomations() {
        return m_config.getAutomations().getAutomationCollection();
    }
    
	/**
	 * Returns a Collection of triggers defined in the config
	 *
	 * @return a {@link java.util.Collection} object.
	 */
	public synchronized Collection<Trigger> getTriggers() {
        return m_config.getTriggers().getTriggerCollection();
    }

    /**
     * Returns a Collection of actions defined in the config
     *
     * @return a {@link java.util.Collection} object.
     */
    public synchronized Collection<Action> getActions() {
        return m_config.getActions().getActionCollection();
    }

    /**
     * Returns a Collection of named events to that may have
     * been configured to be sent after an automation has run.
     *
     * @return a {@link java.util.Collection} object.
     */
    public synchronized Collection<AutoEvent> getAutoEvents() {
        return m_config.getAutoEvents().getAutoEventCollection();
    }

    /**
     * <p>getActionEvents</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    public synchronized Collection<ActionEvent> getActionEvents() {
        return m_config.getActionEvents().getActionEventCollection();
    }

    /**
     * <p>getPeriod</p>
     *
     * @return a int.
     */
    public synchronized int getPeriod() {
        return m_config.getPeriod();
    }

    /**
     * Returns a Trigger with a name matching the string parameter
     *
     * @param triggerName a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.config.vacuumd.Trigger} object.
     */
    public synchronized Trigger getTrigger(String triggerName) {
        for (Trigger trig : getTriggers()) {
            if (trig.getName().equals(triggerName)) {
                return trig;
            }
        }
        return null;
    }
    
    /**
     * Returns an Action with a name matching the string parmater
     *
     * @param actionName a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.config.vacuumd.Action} object.
     */
    public synchronized Action getAction(String actionName) {
        for (Action act : getActions()) {
            if (act.getName().equals(actionName)) {
                return act;
            }
        }
        return null;
    }
    
    /**
     * Returns an Automation with a name matching the string parameter
     *
     * @param autoName a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.config.vacuumd.Automation} object.
     */
    public synchronized Automation getAutomation(String autoName) {
        for (Automation auto : getAutomations()) {
            if (auto.getName().equals(autoName)) {
                return auto;
            }
        }
        return null;
    }
    
    /**
     * Returns the AutoEvent associated with the auto-event-name
     *
     * @deprecated Use {@link ActionEvent} objects instead. Access these objects with {@link #getActionEvent(String)}.
     * @param name a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.config.vacuumd.AutoEvent} object.
     */
    public synchronized AutoEvent getAutoEvent(String name) {
        for (AutoEvent ae : getAutoEvents()) {
            if (ae.getName().equals(name)) {
                return ae;
            }
        }
        return null;
    }

    /**
     * <p>getSqlStatements</p>
     *
     * @return an array of {@link java.lang.String} objects.
     */
    public synchronized String[] getSqlStatements() {
        Statement[] stmts = m_config.getStatement();
        String[] sql = new String[stmts.length];
        for (int i = 0; i < stmts.length; i++) {
            sql[i] = stmts[i].getContent();
        }
        return sql;
    }
    
    /**
     * <p>getStatements</p>
     *
     * @return a {@link java.util.List} object.
     */
    public synchronized List<Statement> getStatements() {
    	return m_config.getStatementCollection();
    }

    /**
     * <p>getActionEvent</p>
     *
     * @param name a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.config.vacuumd.ActionEvent} object.
     */
    public ActionEvent getActionEvent(String name) {
        for (ActionEvent actionEvent : getActionEvents()) {
            if (actionEvent.getName().equals(name)) {
                return actionEvent;
            }
        }
        return null;
    }
}
