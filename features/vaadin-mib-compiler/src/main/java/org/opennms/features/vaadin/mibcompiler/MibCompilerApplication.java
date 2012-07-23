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

import org.opennms.core.xml.JaxbUtils;
import org.opennms.features.vaadin.datacollection.DataCollectionGroupPanel;
import org.opennms.features.vaadin.mibcompiler.api.Logger;
import org.opennms.features.vaadin.mibcompiler.services.MibbleMibParser;
import org.opennms.netmgt.config.DataCollectionConfigDao;
import org.opennms.netmgt.config.datacollection.DatacollectionGroup;
import org.opennms.netmgt.dao.DefaultDataCollectionConfigDao;
import org.springframework.core.io.FileSystemResource;

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

    /* (non-Javadoc)
     * @see com.vaadin.Application#init()
     */
    @Override
    public void init() {
        setTheme(Runo.THEME_NAME);

        final HorizontalSplitPanel mainPanel = new HorizontalSplitPanel();
        final MibConsolePanel mibConsole = new MibConsolePanel();
        final MibTreePanel mibsTree = new MibTreePanel(new MibbleMibParser(), mibConsole); // TODO Find a better way to pass the MIB parser.

        mainPanel.setSizeFull();
        mainPanel.setSplitPosition(25, Sizeable.UNITS_PERCENTAGE);
        mainPanel.addComponent(mibsTree);
        mainPanel.addComponent(mibConsole);

        final Window mainWindow = new Window("MIB Compiler Application", mainPanel);

        /*
        DatacollectionGroup group = JaxbUtils.unmarshal(DatacollectionGroup.class, new FileSystemResource("/opt/syncnms/etc/datacollection/cisco.xml"));
        Logger logger = new Logger() {
            public void error(String message) {
                // TODO Auto-generated method stub
            }
            public void warn(String message) {
                // TODO Auto-generated method stub
            }
            public void info(String message) {
                // TODO Auto-generated method stub
            }
            public void debug(String message) {
                // TODO Auto-generated method stub
            }
        };
        DataCollectionGroupPanel panel = new DataCollectionGroupPanel(group, logger) {
            @Override
            public void cancelProcessing() {
                // TODO Auto-generated method stub
            }
            @Override
            public void generateDataCollectionFile(DatacollectionGroup group) {
                // TODO Auto-generated method stub
            }
        };
        final Window mainWindow = new Window("Data Collection Config", panel);
         */

        setMainWindow(mainWindow);
    }

}
