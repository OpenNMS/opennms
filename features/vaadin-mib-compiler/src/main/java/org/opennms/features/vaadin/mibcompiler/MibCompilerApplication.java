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

import java.io.File;
import java.io.IOException;

import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.utils.LogUtils;
import org.opennms.features.vaadin.mibcompiler.api.MibParser;
import org.opennms.features.vaadin.mibcompiler.services.MibbleMibParser;
import org.opennms.netmgt.config.DefaultEventConfDao;
import org.opennms.netmgt.config.EventConfDao;
import org.springframework.core.io.FileSystemResource;

import com.vaadin.Application;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Runo;

// TODO EventProxy for sending events
// TODO DataCollectionConfigDao for handling changes on datacollection-config.xml
/**
 * The Class MIB Compiler Application.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public class MibCompilerApplication extends Application {

    /** The OpenNMS Event configuration DAO. */
    private EventConfDao eventConfDao;

    /** The MIB parser. */
    private MibParser mibParser;

    /* (non-Javadoc)
     * @see com.vaadin.Application#init()
     */
    @Override
    public void init() {
        setTheme(Runo.THEME_NAME);

        final HorizontalSplitPanel mainPanel = new HorizontalSplitPanel();
        final MibConsolePanel mibConsole = new MibConsolePanel();
        final MibCompilerPanel mibPanel = new MibCompilerPanel(getEventConfDao(), getMibParser(), mibConsole);

        mainPanel.setSizeFull();
        mainPanel.setSplitPosition(25, Sizeable.UNITS_PERCENTAGE);
        mainPanel.addComponent(mibPanel);
        mainPanel.addComponent(mibConsole);

        final Window mainWindow = new Window("MIB Compiler Application", mainPanel);
        setMainWindow(mainWindow);
    }

    /**
     * Gets the OpenNMS Event configuration DAO.
     * <p>This will try to instantiate the manually the eventConfDao in case this application is running through Jetty.</p>
     *
     * @return the OpenNMS Event configuration DAO
     */
    public EventConfDao getEventConfDao() {
        if (eventConfDao == null) {
            File config;
            try {
                config = ConfigFileConstants.getFile(ConfigFileConstants.EVENT_CONF_FILE_NAME);
            } catch (IOException e) {
                throw new RuntimeException("Unable to read events file", e);
            }
            LogUtils.warnf(this, "Instantiating DefaultEventConfDao using events from " + config);
            DefaultEventConfDao dao = new DefaultEventConfDao(); // I need to access the raw event configuration
            dao.setConfigResource(new FileSystemResource(config));
            dao.afterPropertiesSet();
            eventConfDao = dao;
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
     * <p>This will try to instantiate the manually the mibParser in case this application is running through Jetty.</p>
     *
     * @return the MIB Parser
     */
    public MibParser getMibParser() {
        if (mibParser == null) {
            LogUtils.warnf(this, "Instantiating Mibble MibParser");
            mibParser = new MibbleMibParser();
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

}
