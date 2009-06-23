/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified 
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 *
 * 2007 May 21: Java 5 generics and loops, format code, add a few more get<X>s
 *              methods to return a genericized Collection, and reorder a bit.
 *              - dj@opennms.org
 * 2007 Mar 14: Create a public constructor that takes a reader, add a
 *              setInstance method, eliminate setConfigReader method and
 *              m_singleton, and indent a bit. - dj@opennms.org
 *
 * Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.                                                            
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact: 
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.ConfigFileConstants;
import org.opennms.netmgt.config.vacuumd.Action;
import org.opennms.netmgt.config.vacuumd.ActionEvent;
import org.opennms.netmgt.config.vacuumd.AutoEvent;
import org.opennms.netmgt.config.vacuumd.Automation;
import org.opennms.netmgt.config.vacuumd.Statement;
import org.opennms.netmgt.config.vacuumd.Trigger;
import org.opennms.netmgt.config.vacuumd.VacuumdConfiguration;
import org.opennms.netmgt.dao.castor.CastorUtils;
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
     * @param rdr Reader
     * @throws MarshalException
     * @throws ValidationException
     */
    @Deprecated
    public VacuumdConfigFactory(Reader rdr) throws MarshalException, ValidationException {
        m_config = CastorUtils.unmarshal(VacuumdConfiguration.class, rdr);
    }
    
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
     * 
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
     * @return
     */
    public synchronized Collection<Automation> getAutomations() {
        return m_config.getAutomations().getAutomationCollection();
    }
    
    /**
     * Returns a Collection of triggers defined in the config
     * @return 
     */
	public synchronized Collection<Trigger> getTriggers() {
        return m_config.getTriggers().getTriggerCollection();
    }

    /**
     * Returns a Collection of actions defined in the config
     * @return
     */
    public synchronized Collection<Action> getActions() {
        return m_config.getActions().getActionCollection();
    }

    /**
     * Returns a Collection of named events to that may have
     * been configured to be sent after an automation has run.
     */
    public synchronized Collection<AutoEvent> getAutoEvents() {
        return m_config.getAutoEvents().getAutoEventCollection();
    }

    public synchronized Collection<ActionEvent> getActionEvents() {
        return m_config.getActionEvents().getActionEventCollection();
    }

    public synchronized int getPeriod() {
        return m_config.getPeriod();
    }

    /**
     * Returns a Trigger with a name matching the string parameter
     * @param triggerName
     * @return
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
     * @param actionName
     * @return
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
     * @param autoName
     * @return
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
     * @param name
     * @return
     */
    public synchronized AutoEvent getAutoEvent(String name) {
        for (AutoEvent ae : getAutoEvents()) {
            if (ae.getName().equals(name)) {
                return ae;
            }
        }
        return null;
    }

    public synchronized String[] getSqlStatements() {
        Statement[] stmts = m_config.getStatement();
        String[] sql = new String[stmts.length];
        for (int i = 0; i < stmts.length; i++) {
            sql[i] = stmts[i].getContent();
        }
        return sql;
    }
    
    public synchronized List<Statement> getStatements() {
    	return m_config.getStatementCollection();
    }

    public ActionEvent getActionEvent(String name) {
        for (ActionEvent actionEvent : getActionEvents()) {
            if (actionEvent.getName().equals(name)) {
                return actionEvent;
            }
        }
        return null;
    }
}
