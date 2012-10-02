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
package org.opennms.features.vaadin.mibcompiler;

import org.opennms.features.vaadin.mibcompiler.api.MibParser;
import org.opennms.netmgt.config.DataCollectionConfigDao;
import org.opennms.netmgt.config.EventConfDao;
import org.opennms.netmgt.model.events.EventProxy;

import com.vaadin.Application;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Runo;

/**
 * The Class MIB Compiler Application.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public class MibCompilerApplication extends Application {

    /** The OpenNMS Event Proxy. */
    private EventProxy eventProxy;

    /** The OpenNMS Event Configuration DAO. */
    private EventConfDao eventConfDao;

    /** The OpenNMS Data Collection Configuration DAO. */
    private DataCollectionConfigDao dataCollectionDao;

    /** The MIB parser. */
    private MibParser mibParser;

    /* (non-Javadoc)
     * @see com.vaadin.Application#init()
     */
    // TODO Add support for EventProxy and DataCollectionConfigDao on MIB Compiler
    @Override
    public void init() {
        setTheme(Runo.THEME_NAME);

        final HorizontalSplitPanel mainPanel = new HorizontalSplitPanel();
        final MibConsolePanel mibConsole = new MibConsolePanel();
        final MibCompilerPanel mibPanel = new MibCompilerPanel(getEventConfDao(), getEventProxy(), getMibParser(), mibConsole);

        mainPanel.setSizeFull();
        mainPanel.setSplitPosition(25, Sizeable.UNITS_PERCENTAGE);
        mainPanel.addComponent(mibPanel);
        mainPanel.addComponent(mibConsole);

        final Window mainWindow = new Window("MIB Compiler Application", mainPanel);
        setMainWindow(mainWindow);
    }

    /**
     * Gets the OpenNMS Event configuration DAO.
     *
     * @return the OpenNMS Event configuration DAO
     */
    public EventConfDao getEventConfDao() {
        if (eventConfDao == null) {
            throw new RuntimeException("EventConfDao implementation is required.");
        }
        return eventConfDao;
    }

    /**
     * Sets the OpenNMS Event configuration DAO.
     *
     * @param eventConfDao the new OpenNMS Event configuration DAO
     */
    public void setEventConfDao(EventConfDao eventConfDao) {
        this.eventConfDao = eventConfDao;
    }

    /**
     * Gets the MIB Parser.
     *
     * @return the MIB Parser
     */
    public MibParser getMibParser() {
        if (mibParser == null) {
            throw new RuntimeException("MibParser implementation is required.");
        }
        return mibParser;
    }

    /**
     * Sets the MIB Parser.
     *
     * @param mibParser the new MIB Parser
     */
    public void setMibParser(MibParser mibParser) {
        this.mibParser = mibParser;
    }

    /**
     * Gets the OpenNMS Event Proxy.
     *
     * @return the OpenNMS Event Proxy
     */
    public EventProxy getEventProxy() {
        if (eventProxy == null)
            throw new RuntimeException("EventProxy implementation is required.");
        return eventProxy;
    }

    /**
     * Sets the OpenNMS Event Proxy.
     *
     * @param eventConfDao the new OpenNMS Event Proxy
     */
    public void setEventProxy(EventProxy eventProxy) {
        this.eventProxy = eventProxy;
    }

    /**
     * Gets the OpenNMS Data Collection Configuration DAO.
     *
     * @return the OpenNMS Data Collection Configuration DAO
     */
    public DataCollectionConfigDao getDataCollectionDao() {
        if (dataCollectionDao == null)
            throw new RuntimeException("DataCollectionConfigDao implementation is required.");
        return dataCollectionDao;
    }

    /**
     * Sets the OpenNMS Data Collection Configuration DAO.
     *
     * @param eventConfDao the new OpenNMS Data Collection Configuration DAO
     */
    public void setDataCollectionDao(DataCollectionConfigDao dataCollectionDao) {
        this.dataCollectionDao = dataCollectionDao;
    }

}
