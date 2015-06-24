/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.vaadin.datacollection;

import java.io.File;
import java.io.FileWriter;
import java.util.List;

import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.features.mibcompiler.api.MibParser;
import org.opennms.features.mibcompiler.services.PrefabGraphDumper;
import org.opennms.features.vaadin.api.Logger;
import org.opennms.netmgt.config.api.DataCollectionConfigDao;
import org.opennms.netmgt.config.datacollection.DatacollectionGroup;
import org.opennms.netmgt.model.PrefabGraph;
import org.vaadin.dialogs.ConfirmDialog;

import com.vaadin.ui.Window;

/**
 * The Class Data Collection Window.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
//FIXME: When a different group is selected and the current one is being edited, warn about discard the changes or save them before continue
@SuppressWarnings("serial")
public class DataCollectionWindow extends Window {

    /**
     * Instantiates a new data collection window.
     *
     * @param parser the MIB parser
     * @param dataCollectionConfigDao the OpenNMS Data Collection Configuration DAO
     * @param fileName the MIB's file name
     * @param dcGroup the OpenNMS data collection group
     * @param logger the logger object
     * @throws Exception the exception
     */
    public DataCollectionWindow(final MibParser parser, final DataCollectionConfigDao dataCollectionConfigDao, final String fileName, final DatacollectionGroup dcGroup, final Logger logger) throws Exception {
        super(fileName); // Using fileName for as the window's name.
        //setScrollable(true);
        setModal(false);
        setClosable(false);
        setDraggable(false);
        setResizable(false);
        addStyleName("dialog");
        setSizeFull();
        setContent(new DataCollectionGroupPanel(dataCollectionConfigDao, dcGroup, logger, null) {
            @Override
            public void cancel() {
                close();
            }
            @Override
            public void success() {
                ConfirmDialog.show(getUI(),
                                   "Graph Templates",
                                   "Do you want to generate the default graph templates?\nAll the existing templates will be overriden.",
                                   "Yes",
                                   "No",
                                   new ConfirmDialog.Listener() {
                    public void onClose(ConfirmDialog dialog) {
                        if (dialog.isConfirmed()) {
                            generateGraphTemplates(parser, logger);
                        }
                        close();
                    }
                });
            }
            @Override
            public void failure(String reason) {
                close();
            }
        });
    }

    /**
     * Generate graph templates.
     *
     * @param parser the MIB parser
     * @param logger the logger
     */
    public void generateGraphTemplates(final MibParser parser, final Logger logger) {
        final File configDir = new File(ConfigFileConstants.getHome(), "etc" + File.separatorChar + "snmp-graph.properties.d");
        final File file = new File(configDir, parser.getMibName().replaceAll(" ", "_") + "-graph.properties");
        try {
            FileWriter writer = new FileWriter(file);
            List<PrefabGraph> graphs = parser.getPrefabGraphs();
            PrefabGraphDumper dumper = new PrefabGraphDumper();
            dumper.dump(graphs, writer);
            writer.close();
            logger.info("Graph templates successfully generated on " + file);
        } catch (Exception e) {
            logger.error("Can't generate the graph templates on " + file);
        }
    }

}
