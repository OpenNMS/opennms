//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
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
// Tab Size = 8
//

package org.opennms.netmgt.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Category;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.ConfigFileConstants;
import org.opennms.netmgt.config.siteStatusViews.SiteStatusViewConfiguration;
import org.opennms.netmgt.config.siteStatusViews.View;

/**
 * <p>SiteStatusViewsFactory class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class SiteStatusViewsFactory {
    /** The singleton instance. */
    private static SiteStatusViewsFactory m_instance;

    private static boolean m_loadedFromFile = false;

    /** Boolean indicating if the init() method has been called. */
    protected boolean initialized = false;

    /** Timestamp of the viewDisplay file, used to know when to reload from disk. */
    protected static long m_lastModified;

    /** Map of view objects by name. */
    protected static Map<String,View> m_viewsMap;

    private static SiteStatusViewConfiguration m_config;

    /**
     * <p>Constructor for SiteStatusViewsFactory.</p>
     *
     * @param configFile a {@link java.lang.String} object.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @throws java.io.IOException if any.
     */
    public SiteStatusViewsFactory(String configFile) throws MarshalException, ValidationException, IOException {
        
        InputStreamReader rdr = new InputStreamReader(new FileInputStream(configFile));
        
        try {
            initialize(rdr);
        } finally {
            rdr.close();
        }
        
    }

    /**
     * <p>Constructor for SiteStatusViewsFactory.</p>
     *
     * @param rdr a {@link java.io.Reader} object.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public SiteStatusViewsFactory(Reader rdr) throws MarshalException, ValidationException {
        initialize(rdr);
    }

    private void initialize(Reader rdr) throws MarshalException, ValidationException {
        log().debug("initialize: initializing site status views factory.");
        m_config = (SiteStatusViewConfiguration) Unmarshaller.unmarshal(SiteStatusViewConfiguration.class, rdr);

        m_viewsMap = new HashMap<String, View>();
        Collection viewList = m_config.getViews().getViewCollection();
        Iterator i = viewList.iterator();
        while (i.hasNext()) {
            View view = (View) i.next();
            m_viewsMap.put(view.getName(), view);
        }
    }

    /**
     * Be sure to call this method before calling getInstance().
     *
     * @throws java.io.IOException if any.
     * @throws java.io.FileNotFoundException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public static synchronized void init() throws IOException, FileNotFoundException, MarshalException, ValidationException {
        if (m_instance == null) {
            File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.SITE_STATUS_VIEWS_FILE_NAME);
            m_instance = new SiteStatusViewsFactory(cfgFile.getPath());
            m_lastModified = cfgFile.lastModified();
            m_loadedFromFile = true;

        }
    }

    /**
     * Singleton static call to get the only instance that should exist for the
     * ViewsDisplayFactory
     *
     * @return the single views display factory instance
     * @throws java.lang.IllegalStateException
     *             if init has not been called
     */
    public static synchronized SiteStatusViewsFactory getInstance() {
        if (m_instance == null) {
            throw new IllegalStateException("You must call ViewDisplay.init() before calling getInstance().");
        }

        return m_instance;
    }
    
    /**
     * <p>setInstance</p>
     *
     * @param instance a {@link org.opennms.netmgt.config.SiteStatusViewsFactory} object.
     */
    public static synchronized void setInstance(SiteStatusViewsFactory instance) {
        m_instance = instance;
        m_loadedFromFile = false;
    }

    /**
     * <p>reload</p>
     *
     * @throws java.io.IOException if any.
     * @throws java.io.FileNotFoundException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public synchronized void reload() throws IOException, FileNotFoundException, MarshalException, ValidationException {
        m_instance = null;
        init();
    }

    /**
     * Can't be null
     *
     * @param viewName a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.config.siteStatusViews.View} object.
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public View getView(String viewName) throws IOException, MarshalException, ValidationException {
        if (viewName == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        this.updateFromFile();

        View view = (View) m_viewsMap.get(viewName);

        return view;
    }

    /**
     * Reload the viewsdisplay.xml file if it has been changed since we last
     * read it.
     *
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    protected void updateFromFile() throws IOException, MarshalException, ValidationException {
        if (m_loadedFromFile) {
            File siteStatusViewsFile = ConfigFileConstants.getFile(ConfigFileConstants.SITE_STATUS_VIEWS_FILE_NAME);
            if (m_lastModified != siteStatusViewsFile.lastModified()) {
                this.reload();
            }
        }
    }

    /**
     * <p>getConfig</p>
     *
     * @return a {@link org.opennms.netmgt.config.siteStatusViews.SiteStatusViewConfiguration} object.
     */
    public synchronized static SiteStatusViewConfiguration getConfig() {
        return m_config;
    }

    /**
     * <p>setConfig</p>
     *
     * @param m_config a {@link org.opennms.netmgt.config.siteStatusViews.SiteStatusViewConfiguration} object.
     */
    public synchronized static void setConfig(SiteStatusViewConfiguration m_config) {
        SiteStatusViewsFactory.m_config = m_config;
    }

    /**
     * <p>getViewsMap</p>
     *
     * @return a {@link java.util.Map} object.
     */
    public synchronized static Map<String, View> getViewsMap() {
        return m_viewsMap;
    }

    /**
     * <p>setViewsMap</p>
     *
     * @param map a {@link java.util.Map} object.
     */
    public synchronized static void setViewsMap(Map<String, View> map) {
        m_viewsMap = map;
    }
    
    private Category log() {
        return ThreadCategory.getInstance();
    }
}
