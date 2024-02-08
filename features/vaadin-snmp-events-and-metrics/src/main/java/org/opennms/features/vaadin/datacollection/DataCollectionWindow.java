/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
    public DataCollectionWindow(final MibParser parser, final DataCollectionConfigDao dataCollectionConfigDao, final String fileName, final DatacollectionGroup dcGroup, final Logger logger) {
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
