//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2003 Jan 31: Cleaned up some unused imports.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.                                                            
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//       
// For more information contact: 
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//

package org.opennms.netmgt.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Category;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.ConfigFileConstants;
import org.opennms.netmgt.config.monitoringLocations.LocationDef;
import org.opennms.netmgt.config.monitoringLocations.MonitoringLocationsConfiguration;
import org.opennms.netmgt.dao.castor.CastorUtils;

/**
 * 
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * 
 */
public class MonitoringLocationsFactory {
    /** The singleton instance. */
    private static MonitoringLocationsFactory m_instance;

    private static boolean m_loadedFromFile = false;

    /** Boolean indicating if the init() method has been called. */
    protected boolean initialized = false;

    /** Timestamp of the config file, used to know when to reload from disk. */
    protected static long m_lastModified;

    protected static Map<String,LocationDef> m_defsMap;

    private static MonitoringLocationsConfiguration m_config;

    public MonitoringLocationsFactory(String configFile) throws MarshalException, ValidationException, IOException {
        InputStream stream = null;
        try {
            stream = new FileInputStream(configFile);
            initialize(stream);
        } finally {
            if (stream != null) {
                IOUtils.closeQuietly(stream);
            }
        }
    }

    public MonitoringLocationsFactory(InputStream stream) throws MarshalException, ValidationException {
        initialize(stream);
    }

    @Deprecated
    public MonitoringLocationsFactory(Reader rdr) throws MarshalException, ValidationException {
        initialize(rdr);
    }

    private void initialize(InputStream stream) throws MarshalException, ValidationException {
        log().debug("initialize: initializing monitoring locations factory.");
        m_config = CastorUtils.unmarshal(MonitoringLocationsConfiguration.class, stream);
        initializeDefsMap();
    }

    @Deprecated
    private void initialize(Reader rdr) throws MarshalException, ValidationException {
        log().debug("initialize: initializing monitoring locations factory.");
        m_config = CastorUtils.unmarshal(MonitoringLocationsConfiguration.class, rdr);
        initializeDefsMap();
    }

    private void initializeDefsMap() {
        m_defsMap = new HashMap<String, LocationDef>();
        for (LocationDef def : m_config.getLocations().getLocationDefCollection()) {
            m_defsMap.put(def.getLocationName(), def);
        }
    }

    /** Be sure to call this method before calling getInstance(). */
    public static synchronized void init() throws IOException, FileNotFoundException, MarshalException, ValidationException {
        if (m_instance == null) {
            File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.MONITORING_LOCATIONS_FILE_NAME);
            m_instance = new MonitoringLocationsFactory(cfgFile.getPath());
            m_lastModified = cfgFile.lastModified();
            m_loadedFromFile = true;
        }
    }

    /**
     * Singleton static call to get the only instance that should exist for the
     * MonioringLocationsFactory
     * 
     * @return the monitoring locations factory instance
     * @throws IllegalStateException
     *             if init has not been called
     */
    public static synchronized MonitoringLocationsFactory getInstance() {
        if (m_instance == null) {
            throw new IllegalStateException("You must call init() before calling getInstance().");
        }

        return m_instance;
    }
    
    public static synchronized void setInstance(MonitoringLocationsFactory instance) {
        m_instance = instance;
        m_loadedFromFile = false;
    }

    public synchronized void reload() throws IOException, FileNotFoundException, MarshalException, ValidationException {
        m_instance = null;
        init();
    }

    /** Can't be null */
    public LocationDef getDef(String defName) throws IOException, MarshalException, ValidationException {
        if (defName == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        this.updateFromFile();
        LocationDef def = (LocationDef) m_defsMap.get(defName);
        return def;
    }

    /**
     * Reload the config file if it has been changed since we last
     * read it.
     */
    protected void updateFromFile() throws IOException, MarshalException, ValidationException {
        if (m_loadedFromFile) {
            File monitoringLocationsFile = ConfigFileConstants.getFile(ConfigFileConstants.MONITORING_LOCATIONS_FILE_NAME);
            if (m_lastModified != monitoringLocationsFile.lastModified()) {
                this.reload();
            }
        }
    }

    public synchronized static MonitoringLocationsConfiguration getConfig() {
        return m_config;
    }

    public synchronized static void setConfig(MonitoringLocationsConfiguration m_config) {
        MonitoringLocationsFactory.m_config = m_config;
    }

    public synchronized static Map<String, LocationDef> getDefsMap() {
        return m_defsMap;
    }

    public synchronized static void setDefsMap(Map<String, LocationDef> map) {
        m_defsMap = map;
    }
    
    private ThreadCategory log() {
        return ThreadCategory.getInstance();
    }
}
