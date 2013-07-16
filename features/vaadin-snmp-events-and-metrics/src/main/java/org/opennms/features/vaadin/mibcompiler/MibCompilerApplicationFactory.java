/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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
import org.ops4j.pax.vaadin.AbstractApplicationFactory;

import com.vaadin.ui.UI;

/**
 * A factory for creating MibCompilerApplication objects.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
public class MibCompilerApplicationFactory extends AbstractApplicationFactory {

    /** The OpenNMS Event Proxy. */
    private EventProxy eventProxy;

    /** The OpenNMS Event Configuration DAO. */
    private EventConfDao eventConfDao;

    /** The OpenNMS Data Collection Configuration DAO. */
    private DataCollectionConfigDao dataCollectionDao;

    /** The MIB parser. */
    private MibParser mibParser;

    @Override
    public UI getUI() {
        if (eventProxy == null)
            throw new RuntimeException("eventProxy cannot be null.");
        if (eventConfDao == null)
            throw new RuntimeException("eventConfDao cannot be null.");
        if (dataCollectionDao == null)
            throw new RuntimeException("dataCollectionDao cannot be null.");
        if (mibParser == null)
            throw new RuntimeException("mibParser cannot be null.");
        MibCompilerApplication app = new MibCompilerApplication();
        app.setEventProxy(eventProxy);
        app.setEventConfDao(eventConfDao);
        app.setDataCollectionDao(dataCollectionDao);
        app.setMibParser(mibParser);
        return app;
    }

    @Override
    public Class<? extends UI> getUIClass() {
        return MibCompilerApplication.class;
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
     * Sets the OpenNMS Event Proxy.
     *
     * @param eventConfDao the new OpenNMS Event Proxy
     */
    public void setEventProxy(EventProxy eventProxy) {
        this.eventProxy = eventProxy;
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
