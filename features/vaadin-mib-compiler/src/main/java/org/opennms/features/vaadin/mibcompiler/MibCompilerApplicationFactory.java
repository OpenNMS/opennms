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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.opennms.features.vaadin.mibcompiler.api.MibParser;
import org.opennms.netmgt.config.EventConfDao;
import org.ops4j.pax.vaadin.AbstractApplicationFactory;

import com.vaadin.Application;

/**
 * A factory for creating MibCompilerApplication objects.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
public class MibCompilerApplicationFactory extends AbstractApplicationFactory {

    /** The OpenNMS Event configuration DAO. */
    private EventConfDao eventConfDao;

    /** The MIB Parser. */
    private MibParser mibParser;

    /* (non-Javadoc)
     * @see org.ops4j.pax.vaadin.ApplicationFactory#createApplication(javax.servlet.http.HttpServletRequest)
     */
    @Override
    public Application createApplication(HttpServletRequest request) throws ServletException {
        if (eventConfDao == null)
            throw new RuntimeException("eventConfDao cannot be null");
        if (mibParser == null)
            throw new RuntimeException("mibParser cannot be null");
        MibCompilerApplication app = new MibCompilerApplication();
        app.setEventConfDao(eventConfDao);
        app.setMibParser(mibParser);
        return app;
    }

    /* (non-Javadoc)
     * @see org.ops4j.pax.vaadin.ApplicationFactory#getApplicationClass()
     */
    @Override
    public Class<? extends Application> getApplicationClass() throws ClassNotFoundException {
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

}
