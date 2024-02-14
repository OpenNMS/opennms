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
package org.opennms.features.vaadin.mibcompiler;

import org.opennms.features.mibcompiler.api.MibParser;
import org.opennms.features.timeformat.api.TimeformatService;
import org.opennms.netmgt.config.api.DataCollectionConfigDao;
import org.opennms.netmgt.config.api.EventConfDao;
import org.opennms.netmgt.events.api.EventProxy;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.UI;

/**
 * The Class MIB Compiler Application.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@Theme("opennms")
@Title("MIB Compiler Application")
@SuppressWarnings("serial")
public class MibCompilerApplication extends UI {

    /** The OpenNMS Event Proxy. */
    private EventProxy eventProxy;

    /** The OpenNMS Event Configuration DAO. */
    private EventConfDao eventConfDao;

    /** The OpenNMS Data Collection Configuration DAO. */
    private DataCollectionConfigDao dataCollectionDao;

    /** The MIB parser. */
    private MibParser mibParser;

    private TimeformatService timeformatService;

    /**
     * Sets the OpenNMS Event Proxy.
     *
     * @param eventProxy the new OpenNMS event proxy
     */
    public void setEventProxy(EventProxy eventProxy) {
        this.eventProxy = eventProxy;
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
     * Sets the MIB Parser.
     *
     * @param mibParser the new MIB Parser
     */
    public void setMibParser(MibParser mibParser) {
        this.mibParser = mibParser;
    }

    /**
     * Sets the OpenNMS Data Collection Configuration DAO.
     *
     * @param dataCollectionDao the new OpenNMS Data Collection Configuration DAO
     */
    public void setDataCollectionDao(DataCollectionConfigDao dataCollectionDao) {
        this.dataCollectionDao = dataCollectionDao;
    }

    public void setTimeformatService(TimeformatService timeformatService){
        this.timeformatService = timeformatService;
    }

    /* (non-Javadoc)
     * @see com.vaadin.Application#init()
     */
    @Override
    public void init(VaadinRequest request) {
        if (eventProxy == null)
            throw new RuntimeException("eventProxy cannot be null.");
        if (eventConfDao == null)
            throw new RuntimeException("eventConfDao cannot be null.");
        if (dataCollectionDao == null)
            throw new RuntimeException("dataCollectionDao cannot be null.");

        final HorizontalSplitPanel mainPanel = new HorizontalSplitPanel();
        final MibConsolePanel mibConsole = new MibConsolePanel(timeformatService);
        final MibCompilerPanel mibPanel = new MibCompilerPanel(dataCollectionDao, eventConfDao, eventProxy, mibParser, mibConsole);

        mainPanel.setSizeFull();
        mainPanel.setSplitPosition(25, Unit.PERCENTAGE);
        mainPanel.addComponent(mibPanel);
        mainPanel.addComponent(mibConsole);

        setContent(mainPanel);
    }

}
